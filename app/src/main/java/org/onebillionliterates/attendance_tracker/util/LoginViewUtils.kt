package org.onebillionliterates.attendance_tracker.util

import android.R
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.onebillionliterates.attendance_tracker.data.AppData
import org.onebillionliterates.attendance_tracker.data.Teacher


class LoginViewUtils {


    companion object {
        var TAG = this::class.qualifiedName

        private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()

        suspend fun verifyPassCode(
            mobileNumber: String,
            passCode: String
        ): Boolean {
            var flag = false
            var teacher: Teacher? = null

            val job = GlobalScope.launch {
                teacher = AppData.instance.getTeacherInfo(mobileNumber)
            }
            job.join()

            if (teacher != null && teacher!!.passCode==passCode) {
                flag = true
            }
            return flag
        }

        suspend fun verifyAndSavePassCode(
            mobileNumber: String,
            passCode: String
        ): Boolean {

            var flag = false
            var teacher: Teacher?

            val job = GlobalScope.launch {

                teacher = AppData.instance.getTeacherInfo(mobileNumber)

                if (teacher != null) {

                    var credential =
                        PhoneAuthProvider.getCredential(teacher!!.verificationId!!, passCode)

                    println(passCode)

                    mAuth.signInWithCredential(credential)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                GlobalScope.launch {
                                    teacher!!.passCode = passCode
                                    AppData.instance.updateTeacher(teacher!!)
                                }
                                flag = true
                                println("++++++++++ $flag")
                            }
                        }.await()
                }
            }
            job.join()
            println("*************************** $flag")
            return flag
        }
    }

}