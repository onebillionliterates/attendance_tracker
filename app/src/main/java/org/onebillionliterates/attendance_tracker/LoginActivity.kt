package org.onebillionliterates.attendance_tracker

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.login.*
import org.onebillionliterates.attendance_tracker.data.AppData

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)
        setSupportActionBar(toolbar)

        loginButton.setOnClickListener { view ->
            val mobileNumber = "8884410287"
            val passCode = "337703"
            val appData = AppData();

            val userName = username.text.toString()
            val password = password.text.toString()
            if( userName == "admin" && password == "12345"){
                Snackbar.make(view, "Login SuccessFul", Snackbar.LENGTH_LONG).show()
                val intent = Intent(this, AdminLandingActivity::class.java)
                startActivity(intent)
            }

            showLoginFailedDailog()
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
