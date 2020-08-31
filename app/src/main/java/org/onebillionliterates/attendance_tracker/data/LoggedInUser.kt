package org.onebillionliterates.attendance_tracker.data

data class LoggedInUser(
    var adminInfo: Admin? = null,
    var teacherInfo: Teacher? = null
)
