package org.onebillionliterates.attendance_tracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.adapter.SessionAdapter
import org.onebillionliterates.attendance_tracker.data.Session
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class TeacherSessions : AppCompatActivity(), SessionCheckinClick {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teacher_attendance)

        val sessionContainer = findViewById<RecyclerView>(R.id.teacher_sessions_container)
        initSessionContainer(sessionContainer)
    }

    private fun initSessionContainer(sessionContainer : RecyclerView){
        fun createDummySession(description: String): Session {
            return Session(
                adminRef = "",
                schoolRef = "",
                endTime = LocalTime.now(),
                startTime = LocalTime.now(),
                endDate = LocalDate.now(),
                startDate = LocalDate.now(),
                description = description
            )
        }
        sessionContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val sessions = ArrayList<Session>()
        //adding some dummy data to the list
        sessions.add(createDummySession(description = "Session 1"))
        sessions.add(createDummySession(description = "Session 2"))
        sessions.add(createDummySession(description = "Session 3"))

        val adapter = SessionAdapter(sessions, this)
        sessionContainer.adapter = adapter
    }

    override fun onSessionClicked(session: Session) {
        startActivity(Intent(this, CheckInOut::class.java))
    }
}
interface SessionCheckinClick {
    fun onSessionClicked(session: org.onebillionliterates.attendance_tracker.data.Session)
}
