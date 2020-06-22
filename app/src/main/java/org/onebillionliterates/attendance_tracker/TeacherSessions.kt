package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jakewharton.threetenabp.AndroidThreeTen
import com.shasin.notificationbanner.Banner
import kotlinx.android.synthetic.main.teachers_landing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.AdapterListener
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.adapter.ListingAdapter
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.AppCoreException
import org.onebillionliterates.attendance_tracker.data.AppCoreWarnException
import org.onebillionliterates.attendance_tracker.data.MESSAGES

class TeacherSessions : AppCompatActivity(), AdapterListener {
    private val TAG = "TeacherSessions_Activity"
    private lateinit var rootView: View
    private lateinit var sessions: List<DataHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_landing)
        initView()
    }

    private fun initView() {
        rootView = window.decorView.rootView
        uiHandler({
            sessions = AppCore.instance.fetchTeacherSessionsForToday().map { session ->
                DataHolder(
                    id = session.id!!,
                    displayText = session.description
                )
            }
        }, {
            if (!::sessions.isInitialized) {
                return@uiHandler
            }

            if (sessions.isEmpty())
                Banner.make(
                    rootView,
                    this@TeacherSessions,
                    Banner.WARNING,
                    "We did not find any sessions for you today !!!",
                    Banner.TOP
                ).show()

            val sessionContainer = findViewById<RecyclerView>(R.id.teacher_sessions_container)
            sessionContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
            val adapter = ListingAdapter(sessions, true)
            adapter.adapterListener = this
            sessionContainer.adapter = adapter
        })
    }

    private fun uiHandler(beforeBlock: suspend () -> Unit, onUIBlock: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            teacherSessionsProgress.visibility = View.VISIBLE

            var message: String? = null
            var bannerType: Int? = null
            val job = GlobalScope.launch(Dispatchers.IO) {
                try {
                    beforeBlock.invoke()
                } catch (exception: Exception) {
                    bannerType = Banner.ERROR
                    message = MESSAGES.DATA_OPERATION_FAILURE.message
                    if (exception is AppCoreWarnException)
                        bannerType = Banner.WARNING

                    if (exception is AppCoreException || exception is AppCoreWarnException)
                        message = exception.message!!

                    Log.e(TAG, "Error during saving school", exception)
                }
            }
            job.join()
            teacherSessionsProgress.visibility = View.GONE
            if (message != null) {
                Banner.make(rootView, this@TeacherSessions, bannerType!!, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            }
            if (message == null) {
                onUIBlock.invoke()
            }
        }
    }

    override fun onAdapterItemClicked(clickedIndex: Int) {
        startActivity(
            Intent(this, TeacherSessionCheckInCheckOut::class.java).putExtra(
                "selectedSession",
                sessions[clickedIndex].id
            )
        )
    }
}
