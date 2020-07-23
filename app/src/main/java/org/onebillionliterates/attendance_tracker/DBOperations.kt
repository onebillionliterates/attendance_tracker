package org.onebillionliterates.attendance_tracker

import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.all_db_operations.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.onebillionliterates.attendance_tracker.data.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class DBOperations : AppCompatActivity() {


    val TAG = "DBOperations_Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        Firebase.initialize(this)
        AndroidThreeTen.init(this);
        val appData = AppData()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.all_db_operations)

        saveAdmin.setOnClickListener { view ->
            val admin = Admin(
                mobileNumber = "1231231231",
                name = "New Admin",
                passCode = "3344123123",
                remarks = "REPRESENTATION ROW"
            )

            GlobalScope.launch {
                val savedTeacher = appData.saveAdmin(admin)
                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $savedTeacher")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        getAdmin.setOnClickListener { view ->
            GlobalScope.launch {
                val mobileNumber = "1231231231"
                val passCode = "3344123123"
                val admin = appData.getAdminInfo(mobileNumber, passCode)
                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $admin")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }


        saveTeacher.setOnClickListener { view ->
            GlobalScope.launch {
                val teacher = Teacher(
                    id = "id",
                    adminRef = "fw7aJ1dVDpQndyHFhDsv",
                    mobileNumber = "3213213211",
                    name = "Gordon",
                    passCode = "3344123123",
                    remarks = "REPRESENTATION ROW"
                )
                val savedTeacher = appData.saveTeacher(teacher)

                Log.d(TAG, Thread.currentThread().name)
                Log.d(TAG, "PARTICULAR => $savedTeacher")
            }
            Snackbar.make(view, "Check your LogCat-DBOperations_Activity", Snackbar.LENGTH_LONG).show()
        }

        getTeacher.setOnClickListener { view ->
            GlobalScope.launch {
                val teacher = appData.getTeacherInfo("3213213211", passCode = "3344123123")

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
                    uniqueCode = "1231231231",
                    name = "Gordon Ramsay School",
                    postalCode = "3344123123",
                    address = "3344123123",
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
                    startDate = LocalDate.now().minusDays(10),
                    endDate = LocalDate.now().plusDays(40),
                    startTime = LocalTime.NOON.minusHours(1),
                    endTime = LocalTime.NOON,
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
                    startDate = LocalDate.now()
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
                    inTime = LocalTime.now().minusHours(1),
                    outTime = LocalTime.now(),
                    createdAt = LocalDate.now()
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

