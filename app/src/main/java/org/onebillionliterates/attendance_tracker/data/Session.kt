package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDateTime

data class Session(
    val id: String? = null,
    val adminRef: String,
    val schoolRef: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val durationInSecs: Long = 1 * 60 * 60,
    val weekDaysInfo:List<Boolean> = (1..7).map { false },
    val teacherRefs: List<String> = emptyList()
)
