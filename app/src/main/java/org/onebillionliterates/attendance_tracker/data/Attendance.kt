package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

data class Attendance(
    val id: String? = null,
    val adminRef: String,
    val sessionRef: String,
    val teacherRef: String,
    val schoolRef: String,
    val createdAt: LocalDate,
    val inTime: LocalTime,
    val outTime: LocalTime,
    val remarks: String? = null
)
