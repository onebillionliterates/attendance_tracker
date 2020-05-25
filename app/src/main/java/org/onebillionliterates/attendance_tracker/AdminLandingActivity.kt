package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.admin_landing_page.*

class AdminLandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin_landing_page)
        all_schools.setOnClickListener { view ->
            val intent = Intent(this, SchoolListingActivity::class.java)
            startActivity(intent)
        }
        all_teachers.setOnClickListener { view ->
            val intent = Intent(this, TeachersActivity::class.java)
            startActivity(intent)
        }
    }
}
