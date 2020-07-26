package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import org.onebillionliterates.attendance_tracker.data.MESSAGES.*
import org.threeten.bp.Duration
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.LocalTime
import java.math.BigInteger
import java.security.MessageDigest
import kotlin.math.abs

class AppCore {
    private val appData: AppData = AppData.instance
    private var checkedInAttendance: Attendance? = null

    private object HOLDER {
        val INSTANCE = AppCore()
        val LOGGED_IN_USER = LoggedInUser()
    }

    companion object {
        val instance: AppCore by lazy { HOLDER.INSTANCE }
        val loggedInUser: LoggedInUser by lazy { HOLDER.LOGGED_IN_USER }
        val DEFAULT_LOC = LatLng(12.9542946, 77.4908551)
    }

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
            teacherRefs = teachersRefs,
            description = description
        )

    suspend fun saveSession(session: Session): Session {
        session.adminRef = loggedInUser.adminInfo!!.id!!

        if (session.endTime.isBefore(session.startTime)) throw AppCoreException(INVALID_DURATION.message)
        if (session.endTime.toSecondOfDay() - session.startTime.toSecondOfDay() < 30 * 60) throw AppCoreException(
            INVALID_DURATION.message
        )
        if (session.endDate.isBefore(session.startDate)) throw AppCoreException(INVALID_END.message)
        if (session.weekDaysInfo.none { value -> value }) throw AppCoreException(DAYS_NOT_ASSOCIATED.message)
        if (session.schoolRef.isBlank()) throw AppCoreException(SCHOOL_NOT_ASSOCIATED.message)
        if (session.teacherRefs.isNullOrEmpty()) throw AppCoreException(TEACHERS_NOT_ASSOCIATED.message)
        if (runWithCatch { appData.verifySession(session) }) throw AppCoreException(SESSION_ALREADY_EXISTS.message)

        val overlappingSessionsFound = runWithCatch {
            appData.fetchSessions(
                adminRef = session.adminRef,
                schoolRef = session.schoolRef,
                teacherRefs = session.teacherRefs,
                startDate = session.startDate
            )
        }
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

        if (overlappingSessionsFound.isNotEmpty()) throw AppCoreException(SESSIONS_CONFLICTS_EXISTS.message)

        return runWithCatch { appData.saveSession(session) }
    }

    suspend fun allSessions(adminRef: String): Map<String, List<Session>> {
        return mapOf(
            "today" to runWithCatch { appData.fetchActiveSessions(adminRef) }.filter { session ->
                val dayVal = LocalDate.now().dayOfWeek.value - 1
                session.weekDaysInfo[dayVal]
            },
            "past" to runWithCatch { appData.fetchPastSessions(adminRef) },
            "future" to runWithCatch { appData.fetchFutureSessions(adminRef) }
        )
    }

    suspend fun fetchAttendance(
        fromDate: LocalDate,
        toDate: LocalDate
    ): Map<String, Map<LocalDate, List<Session>>> {
        val adminRef: String = loggedInUser.adminInfo!!.id!!

        val allSessionTillToDate = runWithCatch { appData.fetchSessionsTill(adminRef, toDate) }

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
            val attendanceForDay = runWithCatch { appData.fetchAttendanceFor(adminRef, currentDate) }
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

                if (teachersPresent.isNotEmpty()) {
                    val teachersInfo: List<Teacher> =
                        runWithCatch { appData.fetchTeachersForRefs(adminRef, teachersPresent) }
                    presentSessionList[currentDate]!!.add(session.clone(teachersInfo.map { teacher -> teacher.name!! }))
                }
                if (teachersAbsent.isNotEmpty()) {
                    val teachersInfo: List<Teacher> =
                        runWithCatch { appData.fetchTeachersForRefs(adminRef, teachersAbsent) }
                    absentSessionList[currentDate]!!.add(session.clone(teachersInfo.map { teacher -> teacher.name!! }))
                }
            }


        }
        return mapOf("present" to presentSessionList, "absent" to absentSessionList)
    }

    suspend fun login(mobileNumber: String, sixDigitPassCode: String): LoggedInUser {

        val md5Hashing = MessageDigest.getInstance("MD5")
        md5Hashing.update(sixDigitPassCode.toByteArray())
        val hashedPassCode = BigInteger(1, md5Hashing.digest()).toString(16)

        loggedInUser.adminInfo = runWithCatch { appData.getAdminInfo(mobileNumber, hashedPassCode) }
        if (loggedInUser.adminInfo != null)
            return loggedInUser

        loggedInUser.teacherInfo = runWithCatch {
            appData.getTeacherInfo(mobileNumber, hashedPassCode)
        }
        println(loggedInUser)
        return loggedInUser
    }

    suspend fun fetchTeachersForAdmin(): List<Teacher> {
        return runWithCatch {
            appData.fetchTeachers(loggedInUser.adminInfo!!.id!!)
        }
    }

    suspend fun fetchSchoolsForAdmin(): List<School> {
        return runWithCatch {
            appData.fetchSchools(loggedInUser.adminInfo!!.id!!)
        }
    }

    suspend fun fetchTodaysSessionsForAdmin(): List<Session> {
        return runWithCatch {
            appData.fetchActiveSessions(loggedInUser.adminInfo!!.id!!)
        }
    }

    suspend fun fetchFutureSessionsForAdmin(): List<Session> {
        return runWithCatch {
            appData.fetchFutureSessions(loggedInUser.adminInfo!!.id!!)
        }
    }

    suspend fun fetchPastSessionsForAdmin(): List<Session> {
        return runWithCatch {
            appData.fetchPastSessions(loggedInUser.adminInfo!!.id!!)
        }
    }

    suspend fun saveSchool(school: School): School {
        school.adminRef = loggedInUser.adminInfo!!.id.toString()

        if (school.location == null || school.location!!.latitude == DEFAULT_LOC.latitude && school.location!!.longitude == DEFAULT_LOC.longitude) throw AppCoreException(
            SCHOOL_LOCATION_NOT_ADDED.message
        )
        if (school.name.isNullOrBlank() || school.uniqueCode.isNullOrBlank() || school.address.isNullOrBlank() || school.postalCode.isNullOrBlank())
            throw AppCoreException(SCHOOL_INCOMPLETE_INFO.message)

        return runWithCatch {
            appData.saveSchool(school)
        }
    }

    suspend fun checkinToSession(
        teacherCurrentLocation: Location,
        schoolLocation: Location,
        session: Session
    ) {
        val teacherRef = loggedInUser.teacherInfo!!.id!!
        if (checkedInAttendance?.sessionRef != null && checkedInAttendance?.sessionRef == session.id)
            throw AppCoreWarnException(TEACHER_SESSION_ALREADY_CHECKED_IN.message)

        val isSessionAlreadyCheckedId = runWithCatch { appData.findAttendanceForSession(teacherRef, session) }
        if (isSessionAlreadyCheckedId != null) throw AppCoreWarnException(TEACHER_SESSION_ALREADY_CHECKED_IN.message)
        if (teacherCurrentLocation.distanceTo(schoolLocation) > 50) throw AppCoreException(
            TEACHER_SESSION_LOCATION_IS_TO_FAR.message
        )
        val timePassTheSessionStartInMins = (LocalTime.now().toSecondOfDay() - session.startTime.toSecondOfDay()) / 60
        if (abs(timePassTheSessionStartInMins) > 15) throw AppCoreException(TEACHER_SESSION_TIME_MISMATCH_WITH_THRESHOLD.message)

        var forcedCheckout = false
        if (checkedInAttendance?.sessionRef != null && checkedInAttendance?.sessionRef != session.id) {
            forcedCheckout = true
            val previousSession = runWithCatch { appData.fetchSession(checkedInAttendance?.sessionRef!!) }
            checkoutFromSession(previousSession!!, true)
        }

        checkedInAttendance = runWithCatch { appData.checkedInAttendance(teacherRef, session) }

        if (forcedCheckout) throw AppCoreWarnException(TEACHER_SESSION_PREVIOUS_FORCED_CHECKOUT.message)
    }

    suspend fun checkoutFromSession(session: Session, forcedCheckout: Boolean = false) {
        val teacherRef = loggedInUser.teacherInfo!!.id!!
        val checkingOutAttendance = runWithCatch { appData.findAttendanceForSession(teacherRef, session) }
            ?: throw AppCoreWarnException(
                TEACHER_SESSION_NO_SESSION_CHECKED_IN.message
            )

        if (!forcedCheckout && (LocalTime.now()
                .toSecondOfDay() - session.endTime.toSecondOfDay()) < 0
        ) throw AppCoreWarnException(
            TEACHER_SESSION_CHECKOUT_BEFORE_ENDTIME.message
        )
        val updatedAttendance = runWithCatch { appData.checkedOutAttendance(checkingOutAttendance, forcedCheckout) }

        if (checkedInAttendance?.id == updatedAttendance.id) {
            checkedInAttendance = null
        }
    }

    suspend fun fetchSchool(session: Session): School? {
        return runWithCatch { appData.fetchSchool(session) }
    }

    suspend fun fetchSession(sessionRef: String): Session? {
        return runWithCatch { appData.fetchSession(sessionRef) }
    }

    suspend fun fetchTeacherSessionsForToday(): List<Session> {
        val allSessions: List<Session> = runWithCatch {
            appData.fetchTeacherSessionsForToday(
                loggedInUser.teacherInfo!!.adminRef,
                loggedInUser.teacherInfo!!.id!!
            )
        }

        return allSessions.filter { session ->
            session.weekDaysInfo[LocalDate.now().dayOfWeek.value - 1]
        }
    }
}

private suspend fun <T> runWithCatch(block: suspend () -> T): T {
    try {
        val start = System.currentTimeMillis()
        val returnedVal: T = block()

        println("RUN_WITH_CATCH | SUCCESS | Total_Time_Take : ${System.currentTimeMillis() - start}")
        return returnedVal
    } catch (exception: Exception) {
        System.err.println("RUN_WITH_CATCH | FAILED | $exception")
        throw AppCoreException(DATA_OPERATION_FAILURE.message, exception)
    }

}

class AppCoreException(override val message: String, private val exception: Exception? = null) :
    Exception(message, exception)

class AppCoreWarnException(override val message: String, private val exception: Exception? = null) :
    Exception(message, exception)
