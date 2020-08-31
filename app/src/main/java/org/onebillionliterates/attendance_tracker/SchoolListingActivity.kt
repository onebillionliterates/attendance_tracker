package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.school_listing.*
import org.onebillionliterates.attendance_tracker.ActivityRequestCodes.SCHOOL_ADD_ACTIVITY
import org.onebillionliterates.attendance_tracker.ActivityRequestCodes.SCHOOL_UPDATE_ACTIVITY
import org.onebillionliterates.attendance_tracker.adapter.AdapterListener
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.adapter.ListingAdapter
import org.onebillionliterates.attendance_tracker.data.AppCore

class SchoolListingActivity : ActivityUIHandler(), AdapterListener {

    private var allSchools: MutableList<DataHolder> = mutableListOf()
    private var listAdapter: ListingAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_listing)

        super.progressTracker = schoolsProgress
        super.TAG = "SchoolListing_Activity"
        addNewSchool.setOnClickListener {
            val intent = Intent(this, AddSchoolActivity::class.java)
            startActivityForResult(intent, SCHOOL_ADD_ACTIVITY.requestCode)
        }

        initView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SCHOOL_ADD_ACTIVITY.requestCode) {
            uiHandler(
                {
                    loadSchools()
                },
                {
                    listAdapter?.notifyDataSetChanged()
                }
            )
        }
    }

    private fun initView() {
        uiHandler(
            {
                loadSchools()
            },
            {
                initListContainer()
            }
        )
    }

    private suspend fun loadSchools() {
        allSchools.clear()
        allSchools.addAll(AppCore.instance.fetchSchoolsForAdmin().map { school ->
            DataHolder(
                id = school.id!!,
                displayText = school.name!!
            )
        })
    }


    private fun initListContainer() {
        val listContainer = findViewById<RecyclerView>(R.id.school_list_container)
        listContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        listAdapter = ListingAdapter(allSchools, singleSelect = true)
        listAdapter!!.adapterListener = this
        listContainer.adapter = listAdapter
    }

    override fun onAdapterItemClicked(dataHolder: DataHolder) {
        Log.d(TAG, "CLICKED CELL :: ${dataHolder.id} ::")
        
        val intent = Intent(this, AddSchoolActivity::class.java)
        intent.putExtra(SCHOOL_UPDATE_ACTIVITY.name, dataHolder.id)
        startActivityForResult(intent, SCHOOL_UPDATE_ACTIVITY.requestCode)
    }
}
