package org.onebillionliterates.attendance_tracker.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.tasks.TaskExecutors
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppData
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.onebillionliterates.attendance_tracker.model.TeacherBoolean
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit
import kotlin.random.Random


class SmsUtils {

    companion object {

        private lateinit var context: Context
        private lateinit var teacher: Teacher
        private lateinit var teacherBoolean: TeacherBoolean

        private fun setClassVariables(con: Context, t: Teacher, tb: TeacherBoolean) {
            context = con
            teacher = t
            teacherBoolean = tb
        }

        @JvmStatic
        fun sendVerificationCode(con: Context, teacher: Teacher, teacherBoolean: TeacherBoolean) {
            println("sending verfication code")
            setClassVariables(con, teacher, teacherBoolean)
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91" + teacher?.mobileNumber,
                120,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
            )
        }

        private var requestSendSms: Int = 2


        @JvmStatic
        fun sendVerificationCode2(
            activity: Activity,
            con: Context,
            teacher: Teacher,
            teacherBoolean: TeacherBoolean
        ) {
            println("sending verification code from phone")
            setClassVariables(con, teacher, teacherBoolean)

            if (ActivityCompat.checkSelfPermission(
                    con,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                println("no permissions")

                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.SEND_SMS),
                    requestSendSms
                )
            } else {
                println("we have permissions")
                var random = Random(System.currentTimeMillis() / 1000L)
                var text = random.nextInt(100000, 999999).toString()
                sendSms(con, teacher, teacherBoolean, text)
                saveData(text)
            }
        }

        private fun sendSms(con: Context, teacher: Teacher, teacherBoolean: TeacherBoolean, text: String) {
            SmsManager.getDefault()
                .sendTextMessage("+91" + teacher.mobileNumber, null, text, null, null)
            Toast.makeText(
                context,
                "verification code sent to " + teacher.mobileNumber + " successfully",
                Toast.LENGTH_LONG
            ).show()
        }

        private fun saveData(passCode: String) {
            GlobalScope.launch {
                teacher.passCode = passCode
                if (teacherBoolean.isUpdate) {
                    println("update")
                    AppData.instance.updateTeacher(teacher)
                } else {
                    println("save")
                    var t = AppData.instance.saveTeacher(teacher)
                    teacher.id = t.id
                }
            }
        }

        @JvmStatic
        private val mCallbacks: OnVerificationStateChangedCallbacks =
            object : OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                }

                override fun onVerificationFailed(e: FirebaseException) {
                    Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                    super.onCodeSent(s, forceResendingToken)
                    Toast.makeText(
                        context,
                        "verification code sent to " + teacher.mobileNumber + " successfully",
                        Toast.LENGTH_LONG
                    ).show()
                    GlobalScope.launch {
                        teacher.verificationId = s
                        if (teacherBoolean.isUpdate) {
                            println("update")
                            AppData.instance.updateTeacher(teacher)
                        } else {
                            println("save")
                            var t = AppData.instance.saveTeacher(teacher)
                            teacher.id = t.id
                        }
                    }
                }
            }
    }
}



