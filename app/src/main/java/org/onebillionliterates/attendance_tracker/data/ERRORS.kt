package org.onebillionliterates.attendance_tracker.data

enum class ERRORS(val message: String) {
    INVALID_DURATION("Minimum 30 minutes session supported"),
    INVALID_END("Start date cannot be later that end date"),
    SESSION_ALREADY_EXISTS("Already saved sessions exists"),
    SCHOOL_NOT_ASSOCIATED("Session creation needs a school to be associated"),
    DAYS_NOT_ASSOCIATED("Session creation needs days to be associated"),
    TEACHERS_NOT_ASSOCIATED("Session creation needs a teachers to be associated"),
    SESSIONS_CONFLICTS_EXISTS("Conflicting sessions already associated")
}
