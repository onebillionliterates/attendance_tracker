package org.onebillionliterates.attendance_tracker;

import android.os.Bundle;
import android.view.View
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.onebillionliterates.attendance_tracker.adapter.CustomAdapter
import org.onebillionliterates.attendance_tracker.drawables.*
import org.onebillionliterates.attendance_tracker.model.Teacher

class TeachersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.teachers_activity)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerview)
        initRecyclerView(recyclerView)


        val buttonAddNew = findViewById<Button>(R.id.buttonAddNew)
        buttonAddNew.setOnClickListener {
            daysUpDownBottomSheet()
            println("add new clicked")
        }
    }

    private fun initRecyclerView(recyclerView : RecyclerView){
        recyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val teachers = ArrayList<Teacher>()
        //adding some dummy data to the list
        teachers.add(Teacher("Teacher 01", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 02", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 03", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 04", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 05", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 6", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 7", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 8", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 9", "Ranchi Jharkhand"))
        teachers.add(Teacher("Teacher 10", "Ranchi Jharkhand"))

        val adapter = CustomAdapter(teachers)
        recyclerView.adapter = adapter
    }

    private fun initView(view : View) {
        val teacherIcon = view.findViewById<ImageView>(R.id.teacherIcon)
        teacherIcon.setImageDrawable(UserDrawable(this))

        val phoneIcon = view.findViewById<ImageView>(R.id.phoneIcon)
        phoneIcon.setImageDrawable(MobileDrawable(this))

        val emailIdIcon = view.findViewById<ImageView>(R.id.emailIcon)
        emailIdIcon.setImageDrawable(EmailDrawable(this))
    }

    private fun daysUpDownBottomSheet() {
        val bottomSheet = layoutInflater.inflate(R.layout.teachers_drawer, null)
        initView(bottomSheet)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheet)
        dialog.show()
    }
}

