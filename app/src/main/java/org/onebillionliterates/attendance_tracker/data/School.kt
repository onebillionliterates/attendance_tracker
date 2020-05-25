package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import org.threeten.bp.LocalDateTime

class School(
    val id: String? = null,
    val adminRef: String,
    val mobileNumber: String? = null,
    val name: String? = null,
    val uniqueCode: String? = null,
    val remarks: String? = null,
    val createdAt: LocalDateTime? = null,
    val location: Location? = null
)
