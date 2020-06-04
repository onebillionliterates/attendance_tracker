package org.onebillionliterates.attendance_tracker.util

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.drawables.EmailDrawable
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.TrashDrawable
import org.onebillionliterates.attendance_tracker.drawables.UserDrawable
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.model.TeacherBoolean
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset


class TeachersViewUtils {

    companion object {

        fun populateBottomSheet(con: Context, res: Int, teacher: Teacher?) {

            val bottomSheet: View = LayoutInflater.from(con).inflate(res, null)

            initImageIcons(con, bottomSheet)
            initView(con, bottomSheet, teacher)

            val dialog = BottomSheetDialog(con)
            dialog.setContentView(bottomSheet)
            dialog.show()
        }

        var teacherBoolean = TeacherBoolean(false)

        private fun initView(con: Context, view: View, teacher: Teacher?) {

            var editTextTeacher = view.findViewById(R.id.editTextTeacher) as EditText
            var editTextMobile = view.findViewById(R.id.editTextMobile) as EditText
            var editTextEmail = view.findViewById(R.id.editTextEmail) as EditText
            val buttonAdd = view.findViewById(R.id.buttonAdd) as Button

            if(teacher!=null){
                var titleTeacherDrawer = view.findViewById(R.id.titleTeacherDrawer) as TextView
                titleTeacherDrawer.text="Update teacher"

                editTextTeacher.setText(teacher.name)
                editTextMobile.setText(teacher.mobileNumber)
                editTextEmail.setText(teacher.emailId)
                buttonAdd.text = "Update"
            }

            buttonAdd.setOnClickListener {

                if (!isValidMobile(editTextMobile.text.toString())) {
                    var constraintLayout = view.findViewById(R.id.phoneLayout) as ConstraintLayout
                    var phoneAlertLayout =
                        view.findViewById(R.id.phoneAlertLayout) as ConstraintLayout
                    var phoneAlertIcon = view.findViewById(R.id.phoneAlertIcon) as ImageView

                    ValidationUtils.setTextValidations(
                        con,
                        editTextMobile,
                        constraintLayout,
                        phoneAlertLayout,
                        phoneAlertIcon
                    )
                }
                if (!isValidEMail(editTextEmail.text.toString())) {
                    var constraintLayout = view.findViewById(R.id.emailLayout) as ConstraintLayout
                    var emailAlertLayout =
                        view.findViewById(R.id.emailAlertLayout) as ConstraintLayout
                    var emailAlertIcon = view.findViewById(R.id.emailAlertIcon) as ImageView

                    ValidationUtils.setTextValidations(
                        con,
                        editTextEmail,
                        constraintLayout,
                        emailAlertLayout,
                        emailAlertIcon
                    )
                }

                if(teacher==null){
                    val teacher = Teacher(
                        "id",
                        "fw7aJ1dVDpQndyHFhDsv",
                        editTextMobile.text.toString(),
                        editTextTeacher.text.toString(),
                        "qwesdf",
                        "ermarkp with Id",
                        LocalDateTime.now(ZoneOffset.UTC),
                        editTextEmail.text.toString(),
                        "verificationId"
                    )
                    SmsUtils.sendVerificationCode(con, teacher, teacherBoolean)
                } else {
                    teacherBoolean.isUpdate = true
                    println("-----------------------"+teacher)
                    teacher.emailId= editTextEmail.text.toString()
                    teacher.mobileNumber =  editTextMobile.text.toString()
                    teacher.name =  editTextTeacher.text.toString()
                    SmsUtils.sendVerificationCode(con, teacher, teacherBoolean)

                }
            }

        }



        private fun initImageIcons(con: Context, view: View){
            val teacherIcon = view.findViewById<ImageView>(R.id.teacherIcon)
            teacherIcon.setImageDrawable(UserDrawable(con))

            val phoneIcon = view.findViewById<ImageView>(R.id.phoneIcon)
            phoneIcon.setImageDrawable(MobileDrawable(con))

            val emailIdIcon = view.findViewById<ImageView>(R.id.emailIcon)
            emailIdIcon.setImageDrawable(EmailDrawable(con))

            val trashIcon = view.findViewById<ImageView>(R.id.trashIcon)
            val color: Int = Color.parseColor("#ffcc0000") //The color u want
            trashIcon.setColorFilter(color)
            trashIcon.setImageDrawable(TrashDrawable(con))
        }

        private fun isValidEMail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        private fun isValidMobile(phone: String): Boolean {
            return Patterns.PHONE.matcher(phone).matches()
        }
    }
}