package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.onebillionliterates.attendance_tracker.adapter.SessionAdapter
import org.onebillionliterates.attendance_tracker.model.Session

class TeacherSessions : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teacher_attendance)

        val sessionContainer = findViewById<RecyclerView>(R.id.teacher_sessions_container)
        initSessionContainer(sessionContainer)
    }

    private fun initSessionContainer(sessionContainer : RecyclerView){
        sessionContainer.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val sessions = ArrayList<Session>()
        //adding some dummy data to the list
        sessions.add(Session(name = "Session 1"))
        sessions.add(Session(name = "Session 2"))
        sessions.add(Session(name = "Session 3"))

        val adapter = SessionAdapter(sessions)
        sessionContainer.adapter = adapter
    }
}