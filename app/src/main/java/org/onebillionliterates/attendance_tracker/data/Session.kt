package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDateTime

data class Session(
    val id: String? = null,
    val adminRef: String,
    val schoolRef: String,
    val teacherRef: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val durationInSecs: Int = 1 * 60 * 60,
    val mondayWorking: Boolean = false,
    val tuesdayWorking: Boolean = false,
    val wednesdayWorking: Boolean = false,
    val thursdayWorking: Boolean = false,
    val fridayWorking: Boolean = false,
    val saturdayWorking: Boolean = false,
    val sundayWorking: Boolean = false
)
