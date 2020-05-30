package org.onebillionliterates.attendance_tracker

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.all_db_operations.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.onebillionliterates.attendance_tracker.data.*
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
                val teacher = Teacher(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    mobileNumber = "1231231231",
                    name = "Gordon",
                    passCode = "3344123123"
                )
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

        saveSchool.setOnClickListener { view ->
            GlobalScope.launch {
                val location = Location(LocationManager.GPS_PROVIDER)
                location.latitude = 12.9048363
                location.longitude = 77.7007194

                val school = School(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    mobileNumber = "1231231231",
                    name = "Gordon Ramsay School",
                    uniqueCode = "3344123123",
                    location = location
                )
                val savedTeacher = appData.saveSchool(school)

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $savedTeacher")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        getSchool.setOnClickListener { view ->
            GlobalScope.launch {
                val school = appData.getSchoolInfo(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    name = "Gordon Ramsay School",
                    uniqueCode = "3344123123"
                )

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => ${school.id}")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        saveSession.setOnClickListener { view ->
            GlobalScope.launch {
                val session = Session(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    teacherRefs = listOf("s9FoQNUw8cVuJn5JkIxn"),
                    schoolRef = "KI32lNxCTENtUk1z4XDv",
                    startDate = LocalDateTime.now().minusDays(10),
                    endDate = LocalDateTime.now().plusDays(40),
                    durationInSecs = 1 * 60 * 60,
                    weekDaysInfo = listOf(true, false, true, false, true, false, false)
                )

                val savedSession = appData.saveSession(session);

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $savedSession")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        getSession.setOnClickListener { view ->
            GlobalScope.launch {

                val fetchedSession = appData.fetchSessions(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    schoolRef = "KI32lNxCTENtUk1z4XDv",
                    teacherRefs = listOf("s9FoQNUw8cVuJn5JkIxn"),
                    startDate = LocalDateTime.now()
                )

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $fetchedSession")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        saveAttendance.setOnClickListener { view ->
            GlobalScope.launch {
                val attendance = Attendance(
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    teacherRef = "s9FoQNUw8cVuJn5JkIxn",
                    schoolRef = "KI32lNxCTENtUk1z4XDv",
                    sessionRef = "EqQMRPApwAiB5NaxGQdC",
                    inTime = LocalDateTime.now().minusHours(1),
                    outTime = LocalDateTime.now()
                )

                val attendanceSaved = appData.saveAttendance(attendance)

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $attendanceSaved")
                Thread.sleep(4000)
                withContext(Dispatchers.Main) {
                    Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
                }
            }

        }
    }

}

