package org.onebillionliterates.attendance_tracker.data

import org.onebillionliterates.attendance_tracker.data.ERRORS.*
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime

class AppCore(val appData: AppData) {

    fun LocalDate.inBetween(referenceStart: LocalDate, referenceEnd: LocalDate): Boolean =
        referenceStart.isBefore(this) || referenceStart.isEqual(this) &&
                referenceEnd.isAfter(this) || referenceStart.isEqual(this)

    fun LocalTime.inBetween(referenceStart: LocalTime, referenceEnd: LocalTime): Boolean =
        referenceStart.isBefore(this) || referenceStart.equals(this) &&
                referenceEnd.isAfter(this) || referenceStart.equals(this)

    suspend fun saveSession(session: Session) {
        if (session.endTime.toNanoOfDay() - session.startTime.toNanoOfDay() < 30 * 60) throw IllegalArgumentException(
            INVALID_DURATION.message
        )
        if (session.endDate.isBefore(session.startDate)) throw IllegalArgumentException(INVALID_END.message)
        if (session.schoolRef.isBlank()) throw IllegalArgumentException(SCHOOL_NOT_ASSOCIATED.message)
        if (session.teacherRefs.isNullOrEmpty()) throw IllegalArgumentException(TEACHERS_NOT_ASSOCIATED.message)
        if (appData.verifySession(session)) throw IllegalArgumentException(SESSION_ALREADY_EXISTS.message)

        val overlappingSessionsFound = appData.fetchSessions(
            adminRef = session.adminRef,
            schoolRef = session.schoolRef,
            teacherRefs = session.teacherRefs,
            startDate = session.startDate
        )
            .filter {
                    foundSession -> session.startDate.inBetween(foundSession.startDate, foundSession.endDate) ||
                    session.endDate.inBetween(foundSession.startDate, foundSession.endDate)
            }
            .filter { foundSession -> session.startTime.inBetween(foundSession.startTime, foundSession.endTime) ||
                    session.endTime.inBetween(foundSession.startTime, foundSession.endTime)
            }
            .filter { foundSession ->
                (1..7).any { idx ->
                    foundSession.weekDaysInfo[idx] && session.weekDaysInfo[idx]
                }
            }

        if (overlappingSessionsFound.isNotEmpty()) throw IllegalArgumentException(SESSIONS_CONFLICTS_EXISTS.message)

        appData.saveSession(session)
    }

}
