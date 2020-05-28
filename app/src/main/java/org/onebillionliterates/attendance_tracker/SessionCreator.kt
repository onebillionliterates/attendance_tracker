package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.session_create.*
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.onebillionliterates.attendance_tracker.adapter.SessionBottomSheetAdapter
import org.onebillionliterates.attendance_tracker.drawables.*

class SessionCreator : AppCompatActivity(), View.OnClickListener {
    val TAG = "SessionCreator_Activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_create)
        initView()
    }

    private fun initView() {
        schoolIcon.setImageDrawable(SchoolSolidDrawable(this))
        dayIcon.setImageDrawable(CalendarDaySolidDrawable(this))
        teacherIcon.setImageDrawable(UserDrawable(this))

        listOf(R.id.startDate, R.id.endDate)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedDateIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(DateDrawable(this))
                }
            }
        listOf(R.id.startTime, R.id.endTime)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedTimeIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(TimeDrawable(this))
                }
            }
        listOf(R.id.school, R.id.startDate, R.id.endDate, R.id.startTime, R.id.endTime, R.id.days, R.id.teachers)
            .forEach { mainViewId ->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedRightIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(RightIconSolidDrawable(this))
                }
            }

        days.setOnClickListener(this)
        teachers.setOnClickListener(this)
        school.setOnClickListener(this)
    }

    override fun onClick(clickView: View?) {
        when (clickView) {
            days -> {
                openDaysBottomSheet()
            }

            teachers -> {
                openTeachersBottomSheet()
            }

            school -> {
                openSchoolBottomSheet()
            }
        }
    }

    private fun openDaysBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select Days"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
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
        val adapter = SessionBottomSheetAdapter(dataHolder, singleSelect = false)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.show()
    }


    private fun openTeachersBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select Teachers"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //crating an arraylist to store users using the data class user
        val dataHolder = listOf(
            DataHolder("1", "Text 1"),
            DataHolder("2", "Text 2")
        )

        //creating our adapter
        val adapter = SessionBottomSheetAdapter(dataHolder, singleSelect = false)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.show()
    }

    private fun openSchoolBottomSheet() {
        val sheetView = layoutInflater.inflate(R.layout.session_creator_bottom_drawer, null)
        val headerText = sheetView.findViewById<TextView>(R.id.headerText)
        headerText.text = "Select School"
        val recyclerView = sheetView.findViewById<RecyclerView>(R.id.recyclerview)
        //adding a layoutmanager
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        //crating an arraylist to store users using the data class user
        val dataHolder = listOf(
            DataHolder("1", "Text 1"),
            DataHolder("2", "Text 2")
        )

        //creating our adapter
        val adapter = SessionBottomSheetAdapter(dataHolder, singleSelect = true)

        //now adding the adapter to recyclerview
        recyclerView.adapter = adapter

        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)
        dialog.show()
    }
}
