package org.onebillionliterates.attendance_tracker;

import android.os.Bundle;
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.adapter.CustomAdapter
import org.onebillionliterates.attendance_tracker.model.Teacher

class TeachersActivity : AppCompatActivity() {

    private lateinit var linearLayoutManager: LinearLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_teachers)


        //getting recyclerview from xml
        val recyclerView = findViewById(R.id.recyclerview) as RecyclerView

        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)


        //crating an arraylist to store users using the data class user
        val teachers = ArrayList<Teacher>()

        //adding some dummy data to the list
        teachers.add(Teacher("Belal Khan", "Ranchi Jharkhand"))
        teachers.add(Teacher("Ramiz Khan", "Ranchi Jharkhand"))
        teachers.add(Teacher("Faiz Khan", "Ranchi Jharkhand"))
        teachers.add(Teacher("Yashar Khan", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 5", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 6", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 7", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 8", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 9", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 10", "Ranchi Jharkhand"))

        //creating our adapter
        val adapter = CustomAdapter(teachers)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter
    }
}

