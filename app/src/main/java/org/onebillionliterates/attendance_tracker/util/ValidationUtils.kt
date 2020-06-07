package org.onebillionliterates.attendance_tracker.util

import android.content.Context
import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.widget.EditText
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import org.onebillionliterates.attendance_tracker.R
import org.onebillionliterates.attendance_tracker.drawables.AlertDrawable

class ValidationUtils {

    companion object {

        fun isValidEMail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isValidMobile(phone: String): Boolean {
            return Patterns.PHONE.matcher(phone).matches()
        }


        fun setTextValidations(
            context: Context,
            field: EditText,
            baseLayout: ConstraintLayout,
            alertLayout: ConstraintLayout,
            alertIcon: ImageView
        ) {

            baseLayout.setBackgroundResource(R.drawable.button_view_red)

            alertIcon.setImageDrawable(AlertDrawable(context))
            val color = Color.parseColor("#ffcc0000") //The color u want
            alertIcon.setColorFilter(color)

            alertLayout.isVisible = true
            colorChangeAfterClearText(field, baseLayout, alertLayout)
        }

        private fun colorChangeAfterClearText(
            field: EditText, constraintLayout: ConstraintLayout,
            alertLayout: ConstraintLayout
        ) {
            field.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(
                    s: CharSequence,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (s.isEmpty()) {
                        constraintLayout.setBackgroundResource(R.drawable.button_view)
                        alertLayout.isVisible = false
                    }
                }
            })
        }
    }
}