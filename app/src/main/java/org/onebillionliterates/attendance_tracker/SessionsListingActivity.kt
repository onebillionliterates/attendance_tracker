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
import org.onebillionliterates.attendance_tracker.adapter.DataHolder

class SessionsListingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sessions_listing)
        initView()
    }

    private fun initView() {
        val tabLayout = findViewById<TabLayout>(R.id.sessionsTabs)
        val viewPager = findViewById<ViewPager>(R.id.sessionsPager)
        viewPager.adapter = AttendanceListingPager(supportFragmentManager, tabLayout.tabCount)

        findViewById<View>(R.id.addNewSession).setOnClickListener {
            val intent = Intent(this, SessionCreator::class.java)
            startActivity(intent)
        }
    }
}

class SessionListingPager(
    fragmentManger: FragmentManager,
    private val tabCount: Int,
    private val data: Map<String, List<*>>? = null
) :
    FragmentStatePagerAdapter(fragmentManger, tabCount) {
    override fun getItem(position: Int): Fragment {
        when (position) {
            0, 1, 2 -> {
                return AttendanceListFragment()
            }
        }
        throw IllegalStateException("No Tabs Found")
    }

    override fun getCount(): Int {
        return tabCount
    }
}

class SessionsListFragment : Fragment() {
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

    override fun onBindGroupViewHolder(holder: AttendanceSchoolDetails?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder!!.setSchoolDetails(group!! as ExpandableGroup<DataHolder>)
    }
}

