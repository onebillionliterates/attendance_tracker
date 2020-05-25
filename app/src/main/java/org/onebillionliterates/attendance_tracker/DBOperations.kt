package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.all_db_operations.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppData
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.threeten.bp.LocalDateTime

class DBOperations : AppCompatActivity() {

    val appData = AppData()
    val TAG = "DBOperations_Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this);

        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_db_operations)

        getAdmin.setOnClickListener { view ->
            GlobalScope.launch {
                val mobileNumber = "8884410287"
                val passCode = "337703"
                val admin = appData.getAdminInfo(mobileNumber, passCode)
                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $admin")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }


        saveTeacher.setOnClickListener { view ->
            GlobalScope.launch {
                val teacher = Teacher(null, "1231231231", "Gordon", "3344123123", null, LocalDateTime.now())
                val savedTeacher = appData.saveTeacher(teacher)

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $savedTeacher")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        getTeacher.setOnClickListener { view ->
            GlobalScope.launch {
                val teacher = appData.getTeacherInfo("1231231231", "3344123123")

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $teacher")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }
    }

}

