@file:Suppress("UNCHECKED_CAST")

package org.onebillionliterates.attendance_tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
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
import com.thoughtbot.expandablerecyclerview.ExpandableRecyclerViewAdapter
import com.thoughtbot.expandablerecyclerview.models.ExpandableGroup
import com.thoughtbot.expandablerecyclerview.viewholders.ChildViewHolder
import com.thoughtbot.expandablerecyclerview.viewholders.GroupViewHolder
import kotlinx.android.synthetic.main.sessions_listing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.Session

class SessionsListingActivity : AppCompatActivity() {
    private lateinit var todaysSessions: List<Session>
    private lateinit var futureSessions: List<Session>
    private lateinit var pastSessions: List<Session>
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sessions_listing)
        initView()
    }

    private fun initView() {
        val tabLayout = findViewById<TabLayout>(R.id.sessionsTabs)
        val viewPager = findViewById<ViewPager>(R.id.sessionsPager)


        findViewById<View>(R.id.addNewSession).setOnClickListener {
            val intent = Intent(this, SessionCreator::class.java)
            startActivity(intent)
        }

        /**
         * Loading all the data for the drawers
         */
        GlobalScope.launch(Dispatchers.Main) {
            sessionsLoader.visibility = View.VISIBLE

            val job = GlobalScope.launch(Dispatchers.IO) {
                todaysSessions = AppCore.instance.fetchTodaysSessionsForAdmin()
                futureSessions = AppCore.instance.fetchFutureSessionsForAdmin()
                pastSessions = AppCore.instance.fetchPastSessionsForAdmin()
            }
            job.join()
            sessionsLoader.visibility = View.GONE
            viewPager.adapter = SessionListingPager(
                supportFragmentManager, tabLayout.tabCount,
                todays = todaysSessions,
                futures = futureSessions,
                past = pastSessions
            )
        }
    }
}

class SessionListingPager(
    fragmentManger: FragmentManager,
    private val tabCount: Int,
    private val todays: List<Session>,
    private val futures: List<Session>,
    private val past: List<Session>
) :
    FragmentStatePagerAdapter(fragmentManger, tabCount) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0 -> {
                return SessionsListFragment(todays)
            }
            1 -> {
                return SessionsListFragment(futures)
            }
            2 -> {
                return SessionsListFragment(past)
            }
        }
        throw IllegalStateException("No Tabs Found")
    }

    override fun getCount(): Int {
        return tabCount
    }
}

class SessionsListFragment(private val sessions: List<Session>) : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val recyclerView = RecyclerView(requireContext())
        recyclerView.layoutManager = LinearLayoutManager(requireContext(), RecyclerView.VERTICAL, false)
        //crating an arraylist to store users using the data class user
        //creating our adapter

        val groupedSessions = sessions.groupBy { session -> session.schoolRef }
        val expandableList: List<ExpandableGroup<DataHolder>> = groupedSessions.map { entry ->
            val schoolName = entry.value.first().description.split("@")[1]
            val sessions: List<DataHolder> = entry.value.map { session ->
                val sessionTime = session.description.split("@")[0]
                DataHolder(id = session.id!!, displayText = "$sessionTime Session")
            }

            ExpandableGroup(schoolName, sessions)
        }
        val adapter = SessionExpandableListAdapter(requireContext(), expandableList)
        recyclerView.adapter = adapter

        return recyclerView
    }
}

class SchoolDetails(itemView: View) : GroupViewHolder(itemView) {
    fun setSchoolDetails(group: ExpandableGroup<DataHolder>) {
        itemView.findViewById<TextView?>(R.id.sessionName)!!.text = group.title
    }
}

class SessionDetails(itemView: View) : ChildViewHolder(itemView) {
    fun setSessionDetails(session: DataHolder) {
        itemView.findViewById<TextView?>(R.id.displayText)!!.text = session.displayText
    }
}

class SessionExpandableListAdapter(
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

