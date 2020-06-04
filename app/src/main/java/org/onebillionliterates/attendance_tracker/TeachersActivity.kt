package org.onebillionliterates.attendance_tracker;

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.TeachersAdapter
import org.onebillionliterates.attendance_tracker.data.AppData
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.util.DbUtils
import org.onebillionliterates.attendance_tracker.util.TeachersViewUtils


class TeachersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_activity)


        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        initRecyclerView(recyclerView)


        val buttonAddNew = findViewById<Button>(R.id.buttonAddNew)
        buttonAddNew.setOnClickListener {
            TeachersViewUtils.populateBottomSheet(this, R.layout.teachers_drawer, null)
            println("add new clicked")
        }
    }

    private fun initRecyclerView(recyclerView: RecyclerView) {
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        var teachers :MutableList<Teacher> = mutableListOf<Teacher>()

        GlobalScope.launch {
            teachers  = AppData.instance.getTeachersCollection()
            println("-------------"+teachers.size)
            val adapter = TeachersAdapter(teachers)

            this@TeachersActivity.runOnUiThread(java.lang.Runnable {
                recyclerView.adapter = adapter
            })
        }

    }
}

