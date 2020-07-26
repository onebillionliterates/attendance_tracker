package org.onebillionliterates.attendance_tracker

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.View
import android.widget.ImageView
import kotlinx.android.synthetic.main.login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.MESSAGES.UNSUCCESSFUL_LOGIN
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.PasswordDrawable

class LoginActivity : ActivityUIHandler() {
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        super.progressTracker = loginInProgress
        super.activity = this@LoginActivity

        initView()

        login.setOnClickListener { view ->
            uiHandler(
                {
                    val job = GlobalScope.launch(Dispatchers.IO) {
                        val mobileNumber = mobileNumberEditText.text
                        val passCode = passCodeEditText.text
                        AppCore.instance.login(
                            mobileNumber = mobileNumber.toString(),
                            sixDigitPassCode = passCode.toString()
                        )
                    }
                },
                {
                    if (AppCore.loggedInUser.adminInfo == null && AppCore.loggedInUser.teacherInfo == null) {
                        showWarningBanner(UNSUCCESSFUL_LOGIN.message)
                        return@uiHandler
                    }

                    if (AppCore.loggedInUser.adminInfo != null) {
                        intent = Intent(super.activity, AdminLandingActivity::class.java)
                    }

                    if (AppCore.loggedInUser.teacherInfo != null) {
                        intent = Intent(super.activity, TeacherSessions::class.java)
                    }

                    intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP)
                    startActivity(intent)
                    finish()
                }
            )
        }
    }

    private fun initView() {
        val passCodeIcon = findViewById<View>(R.id.passCodeInfo).findViewById<ImageView>(R.id.passCodeIcon)
        passCodeIcon.setImageDrawable(PasswordDrawable(this))

        val phoneIcon = findViewById<View>(R.id.phoneInfo).findViewById<ImageView>(R.id.phoneIcon)
        phoneIcon.setImageDrawable(MobileDrawable(this))

        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val devicePhoneNumber: String? = telephonyManager.line1Number
        if (!devicePhoneNumber.isNullOrBlank()) {
            mobileNumberEditText.isClickable = false
            mobileNumberEditText.isEnabled = false
            mobileNumberEditText.isFocusableInTouchMode = false
            mobileNumberEditText.setText(devicePhoneNumber)
            mobileNumberEditText.setOnEditorActionListener(null)
        }


    }
}
