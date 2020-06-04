package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import info.androidhive.fontawesome.FontTextView
import org.onebillionliterates.attendance_tracker.drawables.DateDrawable

class AdminLandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_landing_page)

        val mainView = listOf(R.id.schools, R.id.sessions, R.id.teachers, R.id.attendance)
        val icons = listOf(
            R.string.fa_school_solid,
            R.string.fa_clipboard_list_solid,
            R.string.fa_chalkboard_teacher_solid,
            R.string.fa_chart_area_solid
        )
        val descriptions = listOf("Schools", "Sessions", "Teachers", "Attendance")
        (0..3).forEach { idx ->
            val mainView = findViewById<View>(mainView[idx])
            val iconView = mainView.findViewById<FontTextView>(R.id.landingIcon)
            iconView.setText(icons[idx])
            val description = mainView.findViewById<TextView>(R.id.landingTextView)
            description.text = descriptions[idx]
        }

        findViewById<View>(R.id.schools).setOnClickListener {
            val intent = Intent(this, SchoolListingActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.teachers).setOnClickListener {
            val intent = Intent(this, TeachersActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.sessions).setOnClickListener {
            val intent = Intent(this, SessionsListingActivity::class.java)
            startActivity(intent)
        }

        findViewById<View>(R.id.attendance).setOnClickListener {
            val intent = Intent(this, AttendanceListingActivity::class.java)
            startActivity(intent)
        }

    }
}
