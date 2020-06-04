package org.onebillionliterates.attendance_tracker.util

import android.content.Context
import android.widget.Toast
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
import java.util.concurrent.TimeUnit


class SmsUtils {

    companion object {

        private lateinit var context: Context
        private lateinit var teacher: Teacher
        private lateinit var teacherBoolean: TeacherBoolean

        private fun setClassVariables(con: Context, t: Teacher, tb: TeacherBoolean) {
            context=con
            teacher=t
            teacherBoolean=tb
        }

        @JvmStatic
        fun sendVerificationCode(con: Context, teacher: Teacher, teacherBoolean: TeacherBoolean) {
            println("sending verfication code")
            setClassVariables(con, teacher, teacherBoolean)
            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+91"+teacher?.mobileNumber,
                60,
                TimeUnit.SECONDS,
                TaskExecutors.MAIN_THREAD,
                mCallbacks
            )
        }

        @JvmStatic
        private val mCallbacks: OnVerificationStateChangedCallbacks =
            object : OnVerificationStateChangedCallbacks() {


                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    //Getting the code sent by SMS
                    val code = phoneAuthCredential.smsCode
                    println("verification code -------- $code")
                    //sometime the code is not detected automatically
                    //in this case the code will be null
                    //so user has to manually enter the code
        //            if (code != null) {
        //                editTextCode.setText(code)
        //                //verifying the code
        //                verifyVerificationCode(code)
        //            }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    //Toast.makeText(context , e.message, Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(s: String, forceResendingToken: ForceResendingToken) {
                    super.onCodeSent(s, forceResendingToken)
                    println("verification Id -------- sms utils ------- $s")
                    //Toast.makeText(context , "$s sent to $mobile successfully", Toast.LENGTH_LONG).show()
                    GlobalScope.launch {
                        teacher.verificationId=s
                        if(teacherBoolean.isUpdate) {
                            AppData.instance.updateTeacher(teacher)
                        }else{
                            AppData.instance.saveTeacher(teacher)
                        }
                    }
                }
            }
    }
}



