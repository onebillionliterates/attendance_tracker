package org.onebillionliterates.attendance_tracker;

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.onebillionliterates.attendance_tracker.adapter.TeachersAdapter
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

        val adapter = TeachersAdapter(teachers)
        recyclerView.adapter = adapter
    }

    private fun initView(view : View) {
        val teacherIcon = view.findViewById<ImageView>(R.id.teacherIcon)
        teacherIcon.setImageDrawable(UserDrawable(this))

        val phoneIcon = view.findViewById<ImageView>(R.id.phoneIcon)
        phoneIcon.setImageDrawable(MobileDrawable(this))

        val emailIdIcon = view.findViewById<ImageView>(R.id.emailIcon)
        emailIdIcon.setImageDrawable(EmailDrawable(this))

        val trashIcon = view.findViewById<ImageView>(R.id.trashIcon)
        val color: Int = Color.parseColor("#ffcc0000") //The color u want
        trashIcon.setColorFilter(color)
        trashIcon.setImageDrawable(TrashDrawable(this))


        var editTextTeacher = view.findViewById(R.id.editTextTeacher) as EditText
        var editTextMobile = view.findViewById(R.id.editTextMobile) as EditText
        var editTextEmail = view.findViewById(R.id.editTextEmail) as EditText
        val buttonAdd = view.findViewById(R.id.buttonAdd) as Button

        buttonAdd.setOnClickListener {

            if(!isValidMobile(editTextMobile.text.toString())){
                var constraintLayout = view.findViewById(R.id.phoneLayout) as ConstraintLayout
                var phoneAlertLayout = view.findViewById(R.id.phoneAlertLayout) as ConstraintLayout
                var phoneAlertIcon = view.findViewById(R.id.phoneAlertIcon) as ImageView

                setTextValidations(editTextMobile, constraintLayout, phoneAlertLayout, phoneAlertIcon)
            }
            if(!isValidEMail(editTextEmail.text.toString())){
                var constraintLayout = view.findViewById(R.id.emailLayout) as ConstraintLayout
                var emailAlertLayout = view.findViewById(R.id.emailAlertLayout) as ConstraintLayout
                var emailAlertIcon = view.findViewById(R.id.emailAlertIcon) as ImageView

                setTextValidations(editTextEmail, constraintLayout, emailAlertLayout, emailAlertIcon)
            }
            println("add clicked")
        }

    }

    private fun setTextValidations(field: EditText, baseLayout: ConstraintLayout, alertLayout: ConstraintLayout,
                                   alertIcon: ImageView){

        baseLayout.setBackgroundResource(R.drawable.button_view_red)

        alertIcon.setImageDrawable(AlertDrawable(this))
        val color = Color.parseColor("#ffcc0000") //The color u want
        alertIcon.setColorFilter(color)

        alertLayout.isVisible=true
        colorChangeAfterClearText(field, baseLayout, alertLayout)
    }


    private fun colorChangeAfterClearText(field: EditText, constraintLayout :ConstraintLayout,
        alertLayout: ConstraintLayout){
        field.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.isEmpty()){
                    constraintLayout.setBackgroundResource(R.drawable.button_view)
                    alertLayout.isVisible=false
                }
            }
        })
    }

    private fun isValidEMail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidMobile(phone: String): Boolean {
        return Patterns.PHONE.matcher(phone).matches()
    }

    private fun daysUpDownBottomSheet() {
        val bottomSheet = layoutInflater.inflate(R.layout.teachers_drawer, null)
        initView(bottomSheet)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheet)
        dialog.show()
    }
}

