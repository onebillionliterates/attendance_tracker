package org.onebillionliterates.attendance_tracker.data

import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime

data class Session(
    val id: String? = null,
    var adminRef: String = "",
    val schoolRef: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val weekDaysInfo: List<Boolean> = (0..6).map { false },
    val teacherRefs: List<String> = emptyList(),
    val description: String = "",
    val createdAt: LocalDateTime = LocalDateTime.now()
)
