package org.onebillionliterates.attendance_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.jakewharton.threetenabp.AndroidThreeTen
import com.shasin.notificationbanner.Banner
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.attendance_listing.*
import kotlinx.android.synthetic.main.check_in_out.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.data.*
import org.threeten.bp.LocalDate

class AttendanceListingActivity : AppCompatActivity() {
    private val TAG = "AttendanceListingActivity"
    private lateinit var rootView: View
    private lateinit var attendanceSessions: Map<String, Map<LocalDate, List<Session>>>
    private lateinit var startDate: LocalDate
    private lateinit var endDate: LocalDate

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.attendance_listing)
        initView()
    }

    private fun initView() {
        rootView = window.decorView.rootView
        startDate = LocalDate.now()
        endDate = LocalDate.now()

        uiHandler(
            {
                attendanceSessions = AppCore.instance.fetchAttendance(startDate, endDate)
            },
            {
                val tabLayout = findViewById<TabLayout>(R.id.attendanceTab)
                val viewPager = findViewById<ViewPager>(R.id.attendancePager)

                viewPager.adapter = AttendanceListingPager(supportFragmentManager, attendanceSessions)

                tabLayout.setupWithViewPager(viewPager)
            }
        )

    }

    private fun uiHandler(beforeBlock: suspend () -> Unit, onUIBlock: suspend () -> Unit) {
        GlobalScope.launch(Dispatchers.Main) {
            attendanceLoader.visibility = View.VISIBLE

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
            attendanceLoader.visibility = View.GONE
            if (message != null) {
                Banner.make(rootView, this@AttendanceListingActivity, bannerType!!, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            }
            if (message == null) {
                onUIBlock.invoke()
            }
        }
    }
}

class AttendanceListingPager(
    fragmentManger: FragmentManager,
    private val data: Map<String, Map<LocalDate, List<Session>>> = emptyMap()
) :
    FragmentStatePagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> AttendanceListFragment(data.getOrElse("present") { emptyMap() })
            else -> AttendanceListFragment(data.getOrElse("absent") { emptyMap() })
        }
    }

    override fun getCount(): Int {
        return 2
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Present"
            else -> {
                return "Absent"
            }
        }
    }
}

class AttendanceListFragment(private val dateSeparatedSessions: Map<LocalDate, List<Session>>) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        //crating an arraylist to store users using the data class user
        //creating our adapter

        val adapter = AttendanceSessionExpandableListAdapter(
            requireContext(),
            listOf(
                ExpandableGroup(
                    "School ", listOf(
                        DataHolder("1", "Session 1 @ Sometime"),
                        DataHolder("2", "Session 1 @ Sometime")
                    )
                ),
                ExpandableGroup(
                    "School 2", listOf(
                        DataHolder("1", "Session 2 @ Sometime"),
                        DataHolder("2", "Session 3 @ Sometime")
                    )
                ),
                ExpandableGroup(
                    "School 3", listOf(
                        DataHolder("1", "Session 4 @ Sometime"),
                        DataHolder("2", "Session 5 @ Sometime")
                    )
                )
            )
        )
        recyclerView.adapter = adapter

        return recyclerView
    }
}

class AttendanceSchoolDetails(itemView: View) : GroupViewHolder(itemView) {
    fun setSchoolDetails(group: ExpandableGroup<DataHolder>) {
        itemView.findViewById<TextView?>(R.id.sessionName)!!.text = group.title
    }
}

class AttendanceSessionDetails(itemView: View) : ChildViewHolder(itemView) {
    fun setSessionDetails(session: DataHolder) {
        itemView.findViewById<TextView?>(R.id.displayText)!!.text = session.displayText
    }
}

class AttendanceSessionExpandableListAdapter(
    private val context: Context,
    groupedData: List<ExpandableGroup<DataHolder>>
) : ExpandableRecyclerViewAdapter<AttendanceSchoolDetails, AttendanceSessionDetails>(groupedData) {
    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): AttendanceSchoolDetails {
        val view = LayoutInflater.from(context).inflate(R.layout.session_title, parent, false)
        return AttendanceSchoolDetails(view);
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): AttendanceSessionDetails {
        val view = LayoutInflater.from(context).inflate(R.layout.session_item, parent, false)
        return AttendanceSessionDetails(view);
    }

    override fun onBindChildViewHolder(
        holder: AttendanceSessionDetails?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        holder!!.setSessionDetails(group!!.items[childIndex] as DataHolder)
    }

    override fun onBindGroupViewHolder(
        holder: AttendanceSchoolDetails?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder!!.setSchoolDetails(group!! as ExpandableGroup<DataHolder>)
    }
}

