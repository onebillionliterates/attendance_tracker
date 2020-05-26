package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.session_create.*
import org.onebillionliterates.attendance_tracker.drawables.*

class SessionCreator : AppCompatActivity() {
    val TAG = "SessionCreator_Activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_create)

        schoolIcon.setImageDrawable(SchoolSolidDrawable(this))
        dayIcon.setImageDrawable(CalendarDaySolidDrawable(this))
        teacherIcon.setImageDrawable(UserDrawable(this))

        listOf(R.id.startDate, R.id.endDate)
            .forEach { mainViewId->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedDateIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(DateDrawable(this))
                }
            }
        listOf(R.id.startTime, R.id.endTime)
            .forEach { mainViewId->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedTimeIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(TimeDrawable(this))
                }
            }
        listOf(R.id.selectSchool, R.id.startDate, R.id.endTime, R.id.startTime, R.id.endTime, R.id.days, R.id.teachers)
            .forEach { mainViewId->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedRightIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(RightIconSolidDrawable(this))
                }
            }

    }
}
