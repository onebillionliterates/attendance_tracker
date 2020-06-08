package org.onebillionliterates.attendance_tracker

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppCore
import org.onebillionliterates.attendance_tracker.data.LoggedInUser
import org.onebillionliterates.attendance_tracker.drawables.MobileDrawable
import org.onebillionliterates.attendance_tracker.drawables.PasswordDrawable

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this);

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        setSupportActionBar(toolbar)

        login.setOnClickListener { view ->
            loginInProgress.visibility = VISIBLE
            var loggedInUser: LoggedInUser = LoggedInUser()
            GlobalScope.launch(Dispatchers.Main) {
                val job = GlobalScope.launch(Dispatchers.IO) {
                    val mobileNumber = mobileNumberEditText.text
                    val passCode = passCodeEditText.text
                    loggedInUser = AppCore.instance.login(
                        mobileNumber = mobileNumber.toString(),
                        sixDigitPassCode = passCode.toString()
                    )
                }
                job.join()
                if (loggedInUser.adminInfo != null) {
                    startActivity(Intent(this@LoginActivity, AdminLandingActivity::class.java))
                }

                if (loggedInUser.teacherInfo != null) {
                    startActivity(Intent(this@LoginActivity, TeacherSessions::class.java))
                }

                loginInProgress.visibility = GONE
            }


        }

        initView()
    }
    private fun initView() {

        val passCodeIcon = findViewById<View>(R.id.passCodeInfo).findViewById<ImageView>(R.id.passCodeIcon)
        passCodeIcon.setImageDrawable(PasswordDrawable(this))

        val phoneIcon = findViewById<View>(R.id.phoneInfo).findViewById<ImageView>(R.id.phoneIcon)
        phoneIcon.setImageDrawable(MobileDrawable(this))

        val telephonyManager = this.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val devicePhoneNumber: String? = telephonyManager.line1Number
        if(!devicePhoneNumber.isNullOrBlank()) {
            mobileNumberEditText.isClickable = false
            mobileNumberEditText.isEnabled = false
            mobileNumberEditText.isFocusableInTouchMode = false
            mobileNumberEditText.setText(devicePhoneNumber)
            mobileNumberEditText.setOnEditorActionListener(null)
        }


    }
    private fun showLoginFailedDailog() {
        val dialog = this?.let {
            // Use the Builder class for convenient dialog construction
            val builder = AlertDialog.Builder(it)
            builder.setMessage("LOGIN FAILED - Use admin/12345")
                .setNegativeButton("CANCEL",
                    DialogInterface.OnClickListener { dialog, id ->
                        // User cancelled the dialog
                    })
            // Create the AlertDialog object and return it
            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null")

        dialog.show();
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
