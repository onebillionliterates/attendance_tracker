package org.onebillionliterates.attendance_tracker.data

import com.google.firebase.Timestamp
import org.onebillionliterates.attendance_tracker.data.ERRORS.*
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset

class AppCore(val appData: AppData) {

    fun LocalDateTime.inBetween(referenceStart: LocalDateTime, referenceEnd: LocalDateTime) : Boolean =
        referenceStart.isBefore(this) || referenceStart.isEqual(this) &&
        referenceEnd.isAfter(this) || referenceStart.isEqual(this)


    suspend fun saveSession(session: Session) {
        if (session.durationInSecs < 30 * 60) throw IllegalArgumentException(INVALID_DURATION.message)
        if (session.endDate.isBefore(session.startDate)) throw IllegalArgumentException(INVALID_END.message)
        if(session.schoolRef.isBlank()) throw IllegalArgumentException(SCHOOL_NOT_ASSOCIATED.message)
        if(session.teacherRefs.isNullOrEmpty()) throw IllegalArgumentException(TEACHERS_NOT_ASSOCIATED.message)
        if(appData.verifySession(session)) throw IllegalArgumentException(SESSION_ALREADY_EXISTS.message)

        val overlappingSessionsFound = appData.fetchSessions(
            adminRef = session.adminRef,
            schoolRef = session.schoolRef,
            teacherRefs = session.teacherRefs,
            startDate = session.startDate
        )
            .filter { foundSession->session.startDate.inBetween(foundSession.startDate, foundSession.endDate) }
            .filter { foundSession ->
                (1..7).any { idx ->
                    foundSession.weekDaysInfo[idx] && session.weekDaysInfo[idx]
                }
            }

        if(overlappingSessionsFound.isNotEmpty())  throw IllegalArgumentException(SESSIONS_CONFLICTS_EXISTS.message)

    }

}
