package org.onebillionliterates.attendance_tracker

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.drawables.*
import org.onebillionliterates.attendance_tracker.util.LoginViewUtils

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidThreeTen.init(this);

        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        setSupportActionBar(toolbar)

        initView()

        login.setOnClickListener { view ->
            GlobalScope.launch {

                var mobileNumber = mobileNumberEditText.text.toString()
                var passCode = passCodeEditText.text.toString()

                if (mobileNumber != "admin") {
                    var flag = LoginViewUtils.verifyAndSavePassCode(mobileNumber, passCode)

                    this@LoginActivity.runOnUiThread(java.lang.Runnable {
                        if (!flag) {
                            Toast.makeText(
                                this@LoginActivity,
                                "invalid mobile number/pass code",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val intent =
                                Intent(this@LoginActivity, AdminLandingActivity::class.java)
                            startActivity(intent)
                        }
                    })
                } else {
                    val intent =
                        Intent(this@LoginActivity, AdminLandingActivity::class.java)
                    startActivity(intent)
                }

            }
        }
    }

    lateinit var mobileNumberEditText: EditText
    lateinit var passCodeEditText: EditText

    private fun initView() {

        val passCodeIcon =
            findViewById<View>(R.id.passCodeInfo).findViewById<ImageView>(R.id.passCodeIcon)
        passCodeIcon.setImageDrawable(PasswordDrawable(this))

        val phoneIcon = findViewById<View>(R.id.phoneInfo).findViewById<ImageView>(R.id.phoneIcon)
        phoneIcon.setImageDrawable(MobileDrawable(this))

        mobileNumberEditText =
            findViewById<View>(R.id.phoneInfo).findViewById<EditText>(R.id.mobileNumberEditText)
        passCodeEditText =
            findViewById<View>(R.id.passCodeInfo).findViewById<EditText>(R.id.passCodeEditText)
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
