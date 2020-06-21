package org.onebillionliterates.attendance_tracker.data

data class LoggedInUser(
    var adminInfo: Admin? = Admin(id="fw7aJ1dVDpQndyHFhDsv"),
    var teacherInfo: Teacher? = Teacher(id="bCrqum4zLUOJdYUT2xsX", adminRef = "fw7aJ1dVDpQndyHFhDsv")
)
