package org.onebillionliterates.attendance_tracker.data

import com.google.firebase.Timestamp
import org.threeten.bp.LocalDateTime

data class Teacher(
    var id: String? = null,
    val adminRef: String,
    var mobileNumber: String? = null,
    var name: String? = null,
    val passCode: String? = null,
    val remarks: String? = null,
    var createdAt: LocalDateTime? = null,
    var emailId: String? = null,
    var verificationId: String? = null
)
