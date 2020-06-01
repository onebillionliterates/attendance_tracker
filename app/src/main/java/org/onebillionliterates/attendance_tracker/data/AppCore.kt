package org.onebillionliterates.attendance_tracker.data

import org.onebillionliterates.attendance_tracker.data.ERRORS.*
import org.threeten.bp.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class AppCore(val appData: AppData) {

    fun LocalDate.inBetween(referenceStart: LocalDate, referenceEnd: LocalDate): Boolean =
        referenceStart.isBefore(this) || referenceStart.isEqual(this) &&
                referenceEnd.isAfter(this) || referenceStart.isEqual(this)

    fun LocalTime.inBetween(referenceStart: LocalTime, referenceEnd: LocalTime): Boolean =
        referenceStart.isBefore(this) && referenceEnd.isAfter(this)

    fun Session.clone(teachersRefs: List<String>) =
        Session(
            id = id,
            adminRef = adminRef,
            schoolRef = schoolRef,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            teacherRefs = teachersRefs
        )

    suspend fun saveSession(session: Session) {
        if (session.endTime.toNanoOfDay() - session.startTime.toNanoOfDay() < 30 * 60) throw IllegalArgumentException(
            INVALID_DURATION.message
        )
        if (session.endDate.isBefore(session.startDate)) throw IllegalArgumentException(INVALID_END.message)
        if (session.weekDaysInfo.none { value -> value }) throw IllegalArgumentException(DAYS_NOT_ASSOCIATED.message)
        if (session.schoolRef.isBlank()) throw IllegalArgumentException(SCHOOL_NOT_ASSOCIATED.message)
        if (session.teacherRefs.isNullOrEmpty()) throw IllegalArgumentException(TEACHERS_NOT_ASSOCIATED.message)
        if (appData.verifySession(session)) throw IllegalArgumentException(SESSION_ALREADY_EXISTS.message)

        val overlappingSessionsFound = appData.fetchSessions(
            adminRef = session.adminRef,
            schoolRef = session.schoolRef,
            teacherRefs = session.teacherRefs,
            startDate = session.startDate
        )
            .filter { foundSession ->
                session.startDate.inBetween(foundSession.startDate, foundSession.endDate) ||
                        session.endDate.inBetween(foundSession.startDate, foundSession.endDate)
            }
            .filter { foundSession ->
                foundSession.startTime.inBetween(session.startTime, session.endTime) ||
                        foundSession.endTime.inBetween(session.startTime, session.endTime) ||
                        session.startTime.inBetween(foundSession.startTime, foundSession.endTime) ||
                        session.endTime.inBetween(foundSession.startTime, foundSession.endTime) ||
                        ((session.startTime == foundSession.startTime) && (session.endTime == foundSession.endTime))
            }
            .filter { foundSession ->
                (0..6).any { idx ->
                    foundSession.weekDaysInfo[idx] && session.weekDaysInfo[idx]
                }
            }

        if (overlappingSessionsFound.isNotEmpty()) throw IllegalArgumentException(SESSIONS_CONFLICTS_EXISTS.message)

        appData.saveSession(session)
    }

    suspend fun allSessions(adminRef: String): Map<String, List<Session>> {
        return mapOf(
            "today" to appData.fetchActiveSessions(adminRef).filter { session ->
                val dayVal = LocalDate.now().dayOfWeek.value - 1
                session.weekDaysInfo[dayVal]
            },
            "past" to appData.fetchPastSessions(adminRef),
            "future" to appData.fetchFutureSessions(adminRef)
        )
    }

    suspend fun fetchAttendance(
        adminRef: String,
        fromDate: LocalDate,
        toDate: LocalDate
    ): Map<String, Map<LocalDate, List<Session>>> {
        val allSessionTillToDate = appData.fetchSessionsTill(adminRef, toDate)

        val days = Duration.between(
            LocalDateTime.of(fromDate, LocalTime.MIDNIGHT),
            LocalDateTime.of(toDate, LocalTime.MIDNIGHT)
        ).toDays()
        var presentSessionList = HashMap<LocalDate, MutableList<Session>>()
        var absentSessionList = HashMap<LocalDate, MutableList<Session>>()
        (0..days).map { day ->
            val currentDate = fromDate.plusDays(day)

            val activeForDay = allSessionTillToDate
                .filter { session -> currentDate.inBetween(session.startDate, session.endDate) }
                .filter { session -> session.weekDaysInfo[currentDate.dayOfWeek.value - 1] }
            val attendanceForDay = appData.fetchAttendanceFor(adminRef, currentDate)
            var lookUpTable = HashMap<String, Boolean>()
            attendanceForDay.forEach { attendance ->
                lookUpTable["${attendance.sessionRef}_${attendance.teacherRef}_${attendance.createdAt}"] = true
            }

            presentSessionList[currentDate] = ArrayList()
            absentSessionList[currentDate] = ArrayList()

            activeForDay.forEach { session ->
                val teachersPresent =
                    session.teacherRefs.filter { teacherRef -> lookUpTable.containsKey("${session.id}_${teacherRef}_${currentDate}") }
                val teachersAbsent =
                    session.teacherRefs.filter { teacherRef -> !lookUpTable.containsKey("${session.id}_${teacherRef}_${currentDate}") }

                if(teachersPresent.isNotEmpty()) presentSessionList[currentDate]!!.add(session.clone(teachersPresent))
                if(teachersAbsent.isNotEmpty()) absentSessionList[currentDate]!!.add(session.clone(teachersAbsent))
            }


        }
        return mapOf("present" to presentSessionList, "absent" to absentSessionList)
    }

}