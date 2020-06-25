package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.shasin.notificationbanner.Banner
import kotlinx.android.synthetic.main.school_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.ActivityRequestCodes.SCHOOL_ADD_ACTIVITY
import org.onebillionliterates.attendance_tracker.adapter.AdapterListener
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.adapter.ListingAdapter
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.AppCoreException
import org.onebillionliterates.attendance_tracker.data.MESSAGES
import org.onebillionliterates.attendance_tracker.data.School

class SchoolListingActivity : AppCompatActivity(), AdapterListener {

    private var allSchools: MutableList<School> = mutableListOf()
    private var bannerType: Int = Banner.SUCCESS
    private val TAG = "SchoolListing_Activity"
    private lateinit var rootView: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_listing)

        addNewSchool.setOnClickListener {
            val intent = Intent(this, AddSchoolActivity::class.java)
            startActivityForResult(intent, SCHOOL_ADD_ACTIVITY.requestCode)
        }

        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCHOOL_ADD_ACTIVITY.requestCode && resultCode == Activity.RESULT_OK) {
            GlobalScope.launch(Dispatchers.Main) {
                schoolsProgress.visibility = View.VISIBLE

                var message = MESSAGES.SCHOOL_SAVED_MESSAGE.message
                val job = GlobalScope.launch(Dispatchers.IO) {
                    try {
                        allSchools.clear()
                        allSchools.addAll(AppCore.instance.fetchSchoolsForAdmin())
                    } catch (exception: Exception) {
                        bannerType = Banner.ERROR
                        message = MESSAGES.DATA_VALIDATION_FAILURE.message
                        if (exception is AppCoreException)
                            message = exception.message

                        Log.e(TAG, "Error during saving school", exception)
                    }
                }
                job.join()
                schoolsProgress.visibility = View.GONE
                if (bannerType == Banner.ERROR) {
                    Banner.make(rootView, this@SchoolListingActivity, bannerType, message, Banner.TOP).show()
                }
            }
        }
    }

    private fun initView() {
        rootView = window.decorView.rootView

        GlobalScope.launch(Dispatchers.Main) {
            schoolsProgress.visibility = View.VISIBLE

            var message = MESSAGES.SCHOOL_SAVED_MESSAGE.message
            val job = GlobalScope.launch(Dispatchers.IO) {
                try {
                    allSchools.clear()
                    allSchools.addAll(AppCore.instance.fetchSchoolsForAdmin())
                } catch (exception: Exception) {
                    bannerType = Banner.ERROR
                    message = MESSAGES.DATA_VALIDATION_FAILURE.message
                    if (exception is AppCoreException)
                        message = exception.message

                    Log.e(TAG, "Error during saving school", exception)
                }
            }
            job.join()
            schoolsProgress.visibility = View.GONE
            if (bannerType == Banner.ERROR) {
                Banner.make(rootView, this@SchoolListingActivity, bannerType, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            }
            initListContainer()
        }
    }


    private fun initListContainer() {
        val listContainer = findViewById<RecyclerView>(R.id.school_list_container)
        listContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val adapter = ListingAdapter(allSchools.map { school ->
            DataHolder(
                id = school.id!!,
                displayText = school.name!!
            )
        }, singleSelect = true)
        adapter.adapterListener = this
        listContainer.adapter = adapter
    }

    override fun onAdapterItemClicked(clickedIndex: Int) {
        Log.d(TAG, "CLICKED CELL :: $clickedIndex ::")
    }
}
