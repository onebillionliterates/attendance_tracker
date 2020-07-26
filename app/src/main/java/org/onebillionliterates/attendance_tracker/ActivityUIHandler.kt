package org.onebillionliterates.attendance_tracker

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.jakewharton.threetenabp.AndroidThreeTen
import com.shasin.notificationbanner.Banner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.onebillionliterates.attendance_tracker.data.AppCoreException
import org.onebillionliterates.attendance_tracker.data.AppCoreWarnException
import org.onebillionliterates.attendance_tracker.data.MESSAGES

abstract class ActivityUIHandler: AppCompatActivity(){
    private lateinit var rootView: View
    lateinit var progressTracker: ProgressBar
    var TAG: String = "ActivityUIHandler"
    lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidThreeTen.init(this)
    }

    fun uiHandler(beforeBlock: suspend () -> Unit, onUIBlock: suspend () -> Unit) {
        rootView = window.decorView.rootView
        GlobalScope.launch(Dispatchers.Main) {
           progressTracker.visibility = View.VISIBLE

            var message: String? = null
            var bannerType: Int? = null
            val job = GlobalScope.launch(Dispatchers.IO) {
                try {
                    beforeBlock.invoke()
                } catch (exception: Exception) {
                    bannerType = Banner.ERROR
                    message = MESSAGES.DATA_OPERATION_FAILURE.message
                    if (exception is AppCoreWarnException)
                        bannerType = Banner.WARNING

                    if (exception is AppCoreException || exception is AppCoreWarnException)
                        message = exception.message!!

                    Log.e(TAG, "Error Details", exception)
                }
            }
            job.join()
            progressTracker.visibility = View.GONE
            if (message != null) {
                Banner.make(rootView, activity, bannerType!!, message, Banner.TOP).show()
                Banner.getInstance().bannerView.setOnClickListener {
                    Banner.getInstance().dismissBanner()
                }
            }
            if (message == null) {
                onUIBlock.invoke()
            }
        }
    }

    fun showWarningBanner(message:String){
        Banner.make(rootView, activity, Banner.WARNING, message, Banner.TOP).show()
        Banner.getInstance().bannerView.setOnClickListener {
            Banner.getInstance().dismissBanner()
        }
    }

    fun showSuccessBanner(message:String){
        Banner.make(rootView, activity, Banner.SUCCESS, message, Banner.TOP).show()
        Banner.getInstance().bannerView.setOnClickListener {
            Banner.getInstance().dismissBanner()
        }
    }

}
