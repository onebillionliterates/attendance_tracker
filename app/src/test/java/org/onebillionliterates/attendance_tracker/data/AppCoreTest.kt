package org.onebillionliterates.attendance_tracker.data

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.any
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE

class AppCoreTest {
    private val mockedAppData: AppData = Mockito.mock(AppData::class.java)
    private val instance = AppCore(mockedAppData)
    private fun getSession(
        id: String? = null,
        adminRef: String = "adminRef",
        schoolRef: String? = "schoolRef",
        teachersRef: List<String> = listOf("teacherRef"),
        startDate: LocalDate = LocalDate.now(),
        endDate: LocalDate = LocalDate.now(),
        startTime: LocalTime = LocalTime.now(),
        endTime: LocalTime = LocalTime.now().plusMinutes(30),
        weekDaysInfo: List<Boolean> = listOf(true, false, true, false, true, false, false)
    ): Session {
        return Session(
            id = id,
            adminRef = adminRef,
            schoolRef = schoolRef!!,
            startDate = startDate,
            endDate = endDate,
            startTime = startTime,
            endTime = endTime,
            teacherRefs = teachersRef,
            weekDaysInfo = weekDaysInfo
        )
    }

    fun parseISODate(isoDate: String) = LocalDate.parse(isoDate, ISO_LOCAL_DATE)

    @Test
    internal fun minimum_30_mins_session() {
        val session = getSession(endTime = LocalTime.now())

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }

        assertThat(thrown.message, `is`(ERRORS.INVALID_DURATION.message))
    }

    @Test
    internal fun end_is_before_start() {
        val session = getSession(endDate = LocalDate.now().minusDays(1))

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(ERRORS.INVALID_END.message))
    }

    @Test
    internal fun when_no_days_attached() {
        val session = getSession(weekDaysInfo = (0..6).map { false })

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(ERRORS.DAYS_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun when_no_school_attached() {
        val session = getSession(schoolRef = "")

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(ERRORS.SCHOOL_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun when_no_teachers_attached() {
        val session = getSession(teachersRef = emptyList())

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(ERRORS.TEACHERS_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun session_already_exits() {
        val session = getSession()

        runBlocking {
            `when`(mockedAppData.verifySession(session)).thenReturn(true)
        }

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session = session)
            }
        }

        assertThat(thrown.message, `is`(ERRORS.SESSION_ALREADY_EXISTS.message))
    }

    @Test
    internal fun session_within_same_date_window_is_found() {
        val session = getSession(
            startDate = parseISODate("2020-01-15"),
            endDate = parseISODate("2020-01-20"),
            startTime = LocalTime.NOON,
            endTime = LocalTime.NOON.plusHours(1)
        )

        runBlocking {
            Mockito.`when`(mockedAppData.verifySession(session)).thenReturn(false)
        }

        runBlocking {
            `when`(
                mockedAppData.fetchSessions(
                    adminRef = "adminRef",
                    schoolRef = "schoolRef",
                    teacherRefs = listOf("teacherRef"),
                    startDate = session.startDate
                )
            ).thenReturn(
                listOf(
                    // Date covers - time in between
                    getSession(
                        startDate = parseISODate("2020-01-01"),
                        endDate = parseISODate("2020-01-31"),
                        weekDaysInfo = listOf(true, false, true, false, false, false, false),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusMinutes(30)
                    ),
                    // Date in between - time in between
                    getSession(
                        startDate = parseISODate("2020-01-10"),
                        endDate = parseISODate("2020-01-19"),
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = LocalTime.NOON.plusMinutes(30),
                        endTime = LocalTime.NOON.plusHours(1)
                    ),
                    // Date terminal - time overlapping start
                    getSession(
                        startDate = session.startDate,
                        endDate = session.startDate,
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = LocalTime.NOON.minusMinutes(15),
                        endTime = LocalTime.NOON.plusMinutes(15)
                    ),
                    // Date terminal - time encompassing
                    getSession(
                        startDate = session.endDate,
                        endDate = session.endDate,
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = session.startTime.minusHours(1),
                        endTime = session.endTime.plusHours(1)
                    )

                )
            )
        }

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session = session)
            }
        }

        assertThat(thrown.message, `is`(ERRORS.SESSIONS_CONFLICTS_EXISTS.message))
    }

    @Test
    internal fun session_within_same_date_different_window() {
        val session = getSession(
            startDate = parseISODate("2020-01-15"),
            endDate = parseISODate("2020-01-20"),
            startTime = LocalTime.NOON,
            endTime = LocalTime.NOON.plusHours(1)
        )

        runBlocking {
            Mockito.`when`(mockedAppData.verifySession(session)).thenReturn(false)
        }

        runBlocking {
            `when`(
                mockedAppData.fetchSessions(
                    adminRef = "adminRef",
                    schoolRef = "schoolRef",
                    teacherRefs = listOf("teacherRef"),
                    startDate = session.startDate
                )
            ).thenReturn(
                listOf(
                    // Next to Next Session
                    getSession(
                        startDate = session.startDate,
                        endDate = session.endDate,
                        startTime = session.endTime,
                        endTime = session.endTime.plusMinutes(30)
                    ),
                    // Next to Next Session
                    getSession(
                        startDate = session.startDate,
                        endDate = session.endDate,
                        startTime = session.startTime.minusMinutes(30),
                        endTime = session.startTime
                    ),
                    // Sessions on the complimenting days
                    getSession(
                        startDate = session.startDate,
                        endDate = session.endDate,
                        startTime = session.startTime,
                        endTime = session.endTime,
                        weekDaysInfo = session.weekDaysInfo.map { valueForDay -> !valueForDay }
                    )
                )
            )
        }
        runBlocking {
            instance.saveSession(session = session)
            Mockito.verify(mockedAppData).saveSession(session)
        }
    }

    @Test
    internal fun session_groups_having_active_session_for_today() {
        runBlocking {
            `when`(
                mockedAppData.fetchActiveSessions(
                    adminRef = "adminRef"
                )
            ).thenReturn(
                listOf(
                    // Active on all days
                    getSession(weekDaysInfo = (0..6).map { true }),
                    // Not active on any day -- Why are you here ??
                    getSession(weekDaysInfo = (0..6).map { false })
                )
            )
            `when`(
                mockedAppData.fetchFutureSessions(
                    adminRef = "adminRef"
                )
            ).thenReturn(
                listOf(getSession())
            )
            `when`(
                mockedAppData.fetchPastSessions(
                    adminRef = "adminRef"
                )
            ).thenReturn(
                listOf(getSession())
            )

            val sessionsMap: Map<String, List<Session>> = instance.allSessions(adminRef = "adminRef")
            val todaySessions = sessionsMap.get("today")
            val pastSessions = sessionsMap.get("past")
            val futureSessions = sessionsMap.get("future")
            val totalSize = todaySessions!!.size + pastSessions!!.size + futureSessions!!.size
            assertThat(totalSize, `is`(3))

            val grouping = listOf<Session>(getSession(schoolRef = "School 1"), getSession(schoolRef = "School 1"), getSession(schoolRef = "School 2")).groupBy { session->session.schoolRef }
            assertThat((grouping["School 1"] ?: error("")).size, `is`(2))
            assertThat((grouping["School 2"] ?: error("")).size, `is`(1))
        }
    }

    @Test
    internal fun attendance_generation_for_duration() {
        val today = parseISODate("2020-05-15");
        runBlocking {
            val allSession = listOf(
                // Active on all days
                getSession(
                    id = "session1Ref",
                    startDate = today.minusDays(15),
                    endDate = today.plusDays(15),
                    startTime = LocalTime.NOON.minusHours(1),
                    endTime = LocalTime.NOON,
                    weekDaysInfo = (0..6).map { true },
                    teachersRef = listOf("teacher1", "teacher2")
                ),
                // Active on all days
                getSession(
                    id = "session2Ref",
                    startDate = today.minusDays(5),
                    endDate = today.plusDays(15),
                    startTime = LocalTime.NOON,
                    endTime = LocalTime.NOON.plusHours(1),
                    weekDaysInfo = (0..6).map { true },
                    teachersRef = listOf("teacher1", "teacher2", "teacher3")
                ),
                // Not active on any day -- Why are you here ??
                getSession(
                    id = "session3Ref",
                    weekDaysInfo = (0..6).map { false },
                    teachersRef = listOf("teacher1", "teacher2")
                )
            )
            `when`(
                mockedAppData.fetchSessionsTill(
                    adminRef = "adminRef",
                    tillDate = today
                )
            ).thenReturn(
                allSession
            )
            val allAttendance = listOf(
                Attendance(
                    adminRef = allSession[0].adminRef,
                    schoolRef = allSession[0].schoolRef,
                    sessionRef = allSession[0].id!!,
                    teacherRef = "teacher1",
                    createdAt = today,
                    inTime = allSession[0].startTime,
                    outTime = allSession[0].endTime
                ),
                Attendance(
                    adminRef = allSession[0].adminRef,
                    schoolRef = allSession[0].schoolRef,
                    sessionRef = allSession[0].id!!,
                    teacherRef = "teacher2",
                    createdAt = today,
                    inTime = allSession[0].startTime,
                    outTime = allSession[0].endTime
                ),
                Attendance(
                    adminRef = allSession[1].adminRef,
                    schoolRef = allSession[1].schoolRef,
                    sessionRef = allSession[1].id!!,
                    teacherRef = "teacher3",
                    createdAt = today,
                    inTime = allSession[1].startTime,
                    outTime = allSession[1].endTime
                ),
                // Not Expecting because Session 3 is not having weekOfDays expected
                Attendance(
                    adminRef = allSession[2].adminRef,
                    schoolRef = allSession[2].schoolRef,
                    sessionRef = allSession[2].id!!,
                    teacherRef = "teacher3",
                    createdAt = today,
                    inTime = allSession[2].startTime,
                    outTime = allSession[2].endTime
                )

                )

            `when`(
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today
                )
            ).thenReturn(
                allAttendance
            )

            `when`(
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today.minusDays(2)
                )
            ).thenReturn(
                listOf(
                    Attendance(
                        adminRef = allSession[0].adminRef,
                        schoolRef = allSession[0].schoolRef,
                        sessionRef = allSession[0].id!!,
                        teacherRef = "teacher1",
                        createdAt = today.minusDays(2),
                        inTime = allSession[0].startTime,
                        outTime = allSession[0].endTime
                    )
                )
            )

            `when`(
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today.minusDays(1)
                )
            ).thenReturn(
                emptyList()
            )


            val fetchAttendanceForADay =
                instance.fetchAttendance(adminRef = "adminRef", fromDate = today, toDate = today)
            fetchAttendanceForADay
            val fetchAttendanceForADuration =
                instance.fetchAttendance(adminRef = "adminRef", fromDate = today.minusDays(2), toDate = today)
            fetchAttendanceForADuration
        }
    }


}
