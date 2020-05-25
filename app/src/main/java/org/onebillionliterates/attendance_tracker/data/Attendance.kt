package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDateTime

data class Attendance(
    val id: String? = null,
    val adminRef: String,
    val sessionRef: String,
    val teacherRef: String,
    val schoolRef: String,
    val inTime: LocalDateTime,
    val outTime: LocalDateTime,
    val remarks: String? = null
)
