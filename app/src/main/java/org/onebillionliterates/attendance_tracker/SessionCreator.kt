package org.onebillionliterates.attendance_tracker

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.session_create.*
import org.onebillionliterates.attendance_tracker.drawables.*

class SessionCreator : AppCompatActivity(), View.OnClickListener {
    val TAG = "SessionCreator_Activity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.session_create)
        initView()
    }

    private fun initView() {
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
        listOf(R.id.selectSchool, R.id.startDate, R.id.endDate, R.id.startTime, R.id.endTime, R.id.days, R.id.teachers)
            .forEach { mainViewId->
                run {
                    val view = findViewById<View>(mainViewId).findViewById<View>(R.id.includedRightIcon)
                    val iconImageView = view.findViewById<ImageView>(R.id.drawableIcon)
                    iconImageView.setImageDrawable(RightIconSolidDrawable(this))
                }
            }

        days.setOnClickListener(this)
    }

    override fun onClick(clickView: View?) {
        when (clickView) {
            days -> {
                daysUpDownBottomSheet()
            }
        }
    }

    private fun daysUpDownBottomSheet() {
        val bottomSheet = layoutInflater.inflate(R.layout.days_drawer, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(bottomSheet)
        dialog.show()
    }
}
