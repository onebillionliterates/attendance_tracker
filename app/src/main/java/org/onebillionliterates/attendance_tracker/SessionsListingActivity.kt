@file:Suppress("UNCHECKED_CAST")

package org.onebillionliterates.attendance_tracker

import android.app.Activity
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
import org.onebillionliterates.attendance_tracker.ActivityRequestCodes.SESSION_ADD_ACTIVITY
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.Session

class SessionsListingActivity : AppCompatActivity() {
    private var todaysSessions: MutableList<Session> = mutableListOf()
    private var futureSessions: MutableList<Session> = mutableListOf()
    private var pastSessions: MutableList<Session> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sessions_listing)
        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SESSION_ADD_ACTIVITY.requestCode && resultCode == Activity.RESULT_OK) {
            sessionsLoader.visibility = View.VISIBLE
            GlobalScope.launch(Dispatchers.Main) {
                sessionsLoader.visibility = View.VISIBLE

                val job = GlobalScope.launch(Dispatchers.IO) {
                    todaysSessions.clear()
                    futureSessions.clear()
                    pastSessions.clear()
                    todaysSessions.addAll(AppCore.instance.fetchTodaysSessionsForAdmin())
                    futureSessions.addAll(AppCore.instance.fetchFutureSessionsForAdmin())
                    pastSessions.addAll(AppCore.instance.fetchPastSessionsForAdmin())
                }
                job.join()
                sessionsLoader.visibility = View.GONE
            }
        }
    }

    private fun initView() {
        findViewById<View>(R.id.addNewSession).setOnClickListener {
            val intent = Intent(this, SessionCreator::class.java)
            startActivityForResult(intent, SESSION_ADD_ACTIVITY.requestCode)
        }

        /**
         * Loading all the data for the drawers
         */
        GlobalScope.launch(Dispatchers.Main) {
            sessionsLoader.visibility = View.VISIBLE

            val job = GlobalScope.launch(Dispatchers.IO) {
                todaysSessions.clear()
                futureSessions.clear()
                pastSessions.clear()
                todaysSessions.addAll(AppCore.instance.fetchTodaysSessionsForAdmin())
                futureSessions.addAll(AppCore.instance.fetchFutureSessionsForAdmin())
                pastSessions.addAll(AppCore.instance.fetchPastSessionsForAdmin())
            }
            job.join()
            sessionsLoader.visibility = View.GONE
            val tabLayout = findViewById<TabLayout>(R.id.sessionsTabs)
            val viewPager = findViewById<ViewPager>(R.id.sessionsPager)

            viewPager.adapter = SessionListingPager(
                supportFragmentManager,
                todays = todaysSessions,
                futures = futureSessions,
                past = pastSessions
            )

            tabLayout.setupWithViewPager(viewPager)
        }
    }
}

class SessionListingPager(
    fragmentManger: FragmentManager,
    private val todays: List<Session>,
    private val futures: List<Session>,
    private val past: List<Session>
) :
    FragmentStatePagerAdapter(fragmentManger, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> SessionsListFragment(todays)
            1 -> SessionsListFragment(futures)
            else -> SessionsListFragment(past)
        }
    }

    override fun getCount(): Int {
        return 3
    }

    override fun getPageTitle(position: Int): CharSequence {
        return when (position) {
            0 -> "Today"
            1 -> "Upcoming"
            else -> {
                return "Past"
            }
        }
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
) : ExpandableRecyclerViewAdapter<SchoolDetails, SessionDetails>(groupedData) {
    override fun onCreateGroupViewHolder(parent: ViewGroup?, viewType: Int): SchoolDetails {
        val view = LayoutInflater.from(context).inflate(R.layout.session_title, parent, false)
        return SchoolDetails(view);
    }

    override fun onCreateChildViewHolder(parent: ViewGroup?, viewType: Int): SessionDetails {
        val view = LayoutInflater.from(context).inflate(R.layout.session_item, parent, false)
        return SessionDetails(view);
    }

    override fun onBindChildViewHolder(
        holder: SessionDetails?,
        flatPosition: Int,
        group: ExpandableGroup<*>?,
        childIndex: Int
    ) {
        holder!!.setSessionDetails(group!!.items[childIndex] as DataHolder)
    }

    override fun onBindGroupViewHolder(
        holder: SchoolDetails?,
        flatPosition: Int,
        group: ExpandableGroup<*>?
    ) {
        holder!!.setSchoolDetails(group!! as ExpandableGroup<DataHolder>)
    }
}

