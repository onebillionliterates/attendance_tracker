package org.onebillionliterates.attendance_tracker.util

import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.drawables.EmailDrawable
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.TrashDrawable
import org.onebillionliterates.attendance_tracker.drawables.UserDrawable
import org.onebillionliterates.attendance_tracker.model.Position
import org.onebillionliterates.attendance_tracker.model.TeacherBoolean
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneOffset


class TeachersViewUtils {


    interface CustomListListener {
        fun onListChanged(myList: List<Teacher>)
    }

    companion object {
        var TAG = this::class.qualifiedName

        lateinit var editTextTeacher: EditText
        lateinit var editTextMobile: EditText
        lateinit var editTextEmail: EditText
        lateinit var buttonAdd: Button
        lateinit var dialog: BottomSheetDialog

        private lateinit var mListener: CustomListListener
        private lateinit var teachers: MutableList<Teacher>
        private lateinit var pos: Position

        private lateinit var trashIcon: ImageView
        private lateinit var removeText: TextView

        private lateinit var activity: Activity


        fun populateBottomSheet(
            act: Activity,
            con: Context,
            resource: Int,
            teachersList: MutableList<Teacher>,
            index: Int
        ) {
            activity=act
            teachers = teachersList
            pos = Position(index)
            val bottomSheet: View = LayoutInflater.from(con).inflate(resource, null)

            initImageIcons(con, bottomSheet)
            initRemoveLayout(con, bottomSheet)
            initView(con, bottomSheet)

            dialog = BottomSheetDialog(con)
            dialog.setContentView(bottomSheet)
            dialog.show()
        }

        private fun initRemoveLayout(con: Context, view: View) {
            trashIcon = view.findViewById<ImageView>(R.id.trashIcon)
            removeText = view.findViewById(R.id.delete) as TextView
            trashIcon.setOnClickListener {
                removeTeacher(con)
            }
            removeText.setOnClickListener {
                removeTeacher(con)
            }
        }

        private fun removeTeacher(con: Context){
            trashIcon.setImageResource (R.drawable.trash_black);
            removeText.setTextColor(Color.parseColor("#0a0a0a"))
            //teachers.removeAt(pos.index)
//                mListener.onListChanged(teachers)
//                Toast.makeText(con, "deleted the teacher successfully", Toast.LENGTH_LONG).show()
//                Log.d(TAG,"deleted the teacher successfully at position " + pos.index)
//            dialog.dismiss()
        }

        private fun initView(con: Context, view: View) {
            editTextTeacher = view.findViewById(R.id.editTextTeacher) as EditText
            editTextMobile = view.findViewById(R.id.editTextMobile) as EditText
            editTextEmail = view.findViewById(R.id.editTextEmail) as EditText
            buttonAdd = view.findViewById(R.id.buttonAdd) as Button

            customizeBottomSheetTitleAndButtonText(view)

            buttonAdd.setOnClickListener {

                if (!ValidationUtils.isValidMobile(editTextMobile.text.toString())) {
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
                if (!ValidationUtils.isValidEMail(editTextEmail.text.toString())) {
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

                createOrUpdateTeacherAndSendSms(con)
                dialog.dismiss()
            }
        }

        private fun createOrUpdateTeacherAndSendSms(con: Context) {
            val name = editTextTeacher.text.toString()
            val mobile = editTextMobile.text.toString()
            val email = editTextEmail.text.toString()

            var teacherBoolean = TeacherBoolean(false)

            Log.d(TAG, "teachers position.index" + pos.index)

            if (pos.index == -1) {
                val teacher = Teacher(
                    "dummy_id",
                    "fw7aJ1dVDpQndyHFhDsv",
                    mobile,
                    name,
                    "dummy_pass_code",
                    "dummy_remarks",
                    LocalDateTime.now(ZoneOffset.UTC),
                    email,
                    "verificationId"
                )
                Log.d(TAG,"adding to teacher to teachers list. now size is " + teachers.size)
                teachers.add(teacher)
                SmsUtils.sendVerificationCode2(activity, con, teacher, teacherBoolean)
            } else {

                teacherBoolean.isUpdate = true

                Log.d(TAG,"updating to teacher at position " + pos.index)
                teachers[pos.index].emailId = email
                teachers[pos.index].mobileNumber = mobile
                teachers[pos.index].name = name
                SmsUtils.sendVerificationCode2(activity, con, teachers[pos.index], teacherBoolean)
            }
            mListener.onListChanged(teachers)
        }

        private fun customizeBottomSheetTitleAndButtonText(view: View) {
            if (pos.index != -1) {
                var titleTeacherDrawer = view.findViewById(R.id.titleTeacherDrawer) as TextView
                titleTeacherDrawer.text = "Update teacher"

                editTextTeacher.setText(teachers[pos.index].name)
                editTextMobile.setText(teachers[pos.index].mobileNumber)
                editTextEmail.setText(teachers[pos.index].emailId)
                buttonAdd.text = "Update"
            }
        }


        private fun initImageIcons(con: Context, view: View) {
            val teacherIcon = view.findViewById<ImageView>(R.id.teacherIcon)
            teacherIcon.setImageDrawable(UserDrawable(con))

            val phoneIcon = view.findViewById<ImageView>(R.id.phoneIcon)
            phoneIcon.setImageDrawable(MobileDrawable(con))

            val emailIdIcon = view.findViewById<ImageView>(R.id.emailIcon)
            emailIdIcon.setImageDrawable(EmailDrawable(con))
        }

        fun setOnListChangeListener(listener: CustomListListener) {
            mListener = listener
        }
    }
}