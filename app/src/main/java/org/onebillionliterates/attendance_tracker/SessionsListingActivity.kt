@file:Suppress("UNCHECKED_CAST")

package org.onebillionliterates.attendance_tracker

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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
import org.onebillionliterates.attendance_tracker.drawables.ArrowDrawable
import org.onebillionliterates.attendance_tracker.drawables.DownArrowDrawable

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
        viewPager.adapter = SessionListingPager(supportFragmentManager, tabLayout.tabCount)
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
                return SessionsListFragment()
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
        val dataHolder = listOf(
            DataHolder("1", "Monday"),
            DataHolder("2", "Tuesday"),
            DataHolder("3", "Wednesday"),
            DataHolder("4", "Thursday"),
            DataHolder("5", "Friday"),
            DataHolder("6", "Saturday"),
            DataHolder("7", "Sunday")
        )

        //creating our adapter

        val adapter = SessionExpandableListAdapter(
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
        itemView.findViewById<ImageView?>(R.id.sessionDrawable)!!.setImageDrawable(DownArrowDrawable(itemView.context))
    }
}

class SessionDetails(itemView: View) : ChildViewHolder(itemView) {
    fun setSessionDetails(session: DataHolder) {
        itemView.findViewById<TextView?>(R.id.displayText)!!.text = session.displayText
        itemView.findViewById<ImageView?>(R.id.arrowDrawable)!!.setImageDrawable(ArrowDrawable(itemView.context))
    }
}


class SessionExpandableListAdapter(
    private val context: Context,
    private val groupedData: List<ExpandableGroup<DataHolder>>
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

    override fun onBindGroupViewHolder(holder: SchoolDetails?, flatPosition: Int, group: ExpandableGroup<*>?) {
        holder!!.setSchoolDetails(group!! as ExpandableGroup<DataHolder>)
    }


}

