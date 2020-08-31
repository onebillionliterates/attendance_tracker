package org.onebillionliterates.attendance_tracker;

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.adapter.TeachersAdapter
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.AppData
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.util.TeachersViewUtils


class TeachersActivity : AppCompatActivity(), TeachersViewUtils.CustomListListener {

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_activity)
        TeachersViewUtils.setOnListChangeListener(this)

        recyclerView = findViewById<RecyclerView>(R.id.recyclerview)

        GlobalScope.launch {
            var teachers = initRecyclerView()
            val buttonAddNew = findViewById<Button>(R.id.buttonAddNew)
            buttonAddNew.setOnClickListener {
                TeachersViewUtils.populateBottomSheet(
                    this@TeachersActivity,
                    this@TeachersActivity,
                    R.layout.teachers_drawer,
                    teachers,
                    -1
                )
                println("add new clicked")
            }
        }
    }

    private suspend fun initRecyclerView(): MutableList<Teacher> {

        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        var teachers: MutableList<Teacher> = mutableListOf()

        val job = GlobalScope.launch {
            teachers.addAll(AppCore.instance.fetchTeachersForAdmin())
            
            this@TeachersActivity.runOnUiThread(java.lang.Runnable {
                recyclerView.adapter = TeachersAdapter(this@TeachersActivity, teachers)
            })
        }
        job.join()
        return teachers
    }

    override fun onListChanged(teachers: List<Teacher>) {
        recyclerView.adapter?.notifyDataSetChanged()
        recyclerView.scrollToPosition(teachers.size - 1);
    }
}

