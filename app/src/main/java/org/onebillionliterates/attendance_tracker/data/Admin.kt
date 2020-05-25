package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDateTime

data class Admin(
    val id: String? = null,
    val mobileNumber: String? = null,
    val name: String? = null,
    val passCode: String? = null,
    val remarks: String? = null,
    val createdAt: LocalDateTime? = null
)
