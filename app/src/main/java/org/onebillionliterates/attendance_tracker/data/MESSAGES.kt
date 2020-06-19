package org.onebillionliterates.attendance_tracker.data

enum class MESSAGES(val message: String) {
    INVALID_DURATION("Minimum 30 minutes session supported"),
    INVALID_END("Start date cannot be later that end date"),
    SESSION_ALREADY_EXISTS("Already saved sessions exists"),
    SCHOOL_NOT_ASSOCIATED("Session creation needs a school to be associated"),
    DAYS_NOT_ASSOCIATED("Session creation needs days to be associated"),
    TEACHERS_NOT_ASSOCIATED("Session creation needs a teachers to be associated"),
    SESSIONS_CONFLICTS_EXISTS("Conflicting sessions already associated"),
    DATA_OPERATION_FAILURE("Something went wrong, please could you retry!"),
    DATA_VALIDATION_FAILURE("Please fill all details, before trying to create a new session"),
    SCHOOL_SAVED_MESSAGE("New school saved successfully"),
    SESSION_SAVED_MESSAGE("New session created successfully"),
    SCHOOL_LOCATION_NOT_ADDED("School location not added"),
    SCHOOL_INCOMPLETE_INFO("Please fill all details, before trying to save new school")
}