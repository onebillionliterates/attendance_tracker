package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.adapter.SchoolsAdapter
import org.onebillionliterates.attendance_tracker.model.School

class SchoolListingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.school_listing)

        val listContainer = findViewById<RecyclerView>(R.id.school_list_container)
        initListContainer(listContainer)

        val buttonAddNew = findViewById<Button>(R.id.add_school_button)
        buttonAddNew.setOnClickListener {
            val intent = Intent(this, AddSchoolActivity::class.java)
            startActivity(intent)
        }
    }

    private fun initListContainer(listContainer : RecyclerView){
        listContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val dummySchools = ArrayList<School>()
        //adding some dummy data to the list
        dummySchools.add(School(name = "Public School", phoneNumber = "987654321"))
        dummySchools.add(School(name = "DPS School", phoneNumber = "7865945534"))
        dummySchools.add(School(name = "Private School", phoneNumber = "123455657"))
        dummySchools.add(School(name = "International School", phoneNumber = "456723456"))
        dummySchools.add(School(name = "Government School", phoneNumber = "7895834567"))

        val adapter = SchoolsAdapter(dummySchools)
        listContainer.adapter = adapter
    }
}
