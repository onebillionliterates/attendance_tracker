package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import io.mockk.*
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.onebillionliterates.attendance_tracker.adapter.DataHolder
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE

@ExtendWith(MockKExtension::class)
class AppCoreTest {
    private lateinit var mockedAppData: AppData
    private lateinit var instance: AppCore

    @MockK
    private lateinit var schoolLocation: Location

    @MockK
    private lateinit var teacherCurrentLocation: Location

    private fun getSession(
        id: String? = null,
        adminRef: String = ADMIN_REF,
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

    private fun getAttendance(
        id: String? = null,
        adminRef: String = ADMIN_REF,
        schoolRef: String? = "schoolRef",
        sessionRef: String = "sessionRef",
        inTime: LocalTime = LocalTime.now(),
        outTime: LocalTime? = null,
        createdAt: LocalDate = LocalDate.now(),
        forceLoggedOut: Boolean = false
    ): Attendance {
        return Attendance(
            id = id,
            adminRef = adminRef,
            schoolRef = schoolRef!!,
            sessionRef = sessionRef,
            teacherRef = TEACHER_REF,
            inTime = inTime,
            outTime = outTime,
            createdAt = createdAt,
            forceLoggedOut = forceLoggedOut
        )
    }

    fun parseISODate(isoDate: String) = LocalDate.parse(isoDate, ISO_LOCAL_DATE)

    @BeforeEach
    internal fun setUp() {
        mockedAppData = mockk<AppData>()
        mockkObject(AppData)
        every { AppData.instance } returns mockedAppData
        instance = AppCore()
        // Logging-in before every test
        runBlocking {
            coEvery {
                mockedAppData.getAdminInfo(
                    "11111111",
                    "7fa8282ad93047a4d6fe6111c93b308a"
                )
            } returns Admin(id = ADMIN_REF)
            instance.login("11111111", "1111111")
        }

        every { teacherCurrentLocation.distanceTo(schoolLocation) } returns 0f
        coEvery {
            mockedAppData.findAttendanceForSession(any())
        } returns null
    }

    @Test
    internal fun minimum_30_mins_session() {
        val session = getSession(endTime = LocalTime.now())

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }

        assertThat(thrown.message, `is`(MESSAGES.INVALID_DURATION.message))
    }

    @Test
    internal fun end_is_before_start() {
        val session = getSession(endDate = LocalDate.now().minusDays(1))

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(MESSAGES.INVALID_END.message))
    }

    @Test
    internal fun when_no_days_attached() {
        val session = getSession(weekDaysInfo = (0..6).map { false })

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(MESSAGES.DAYS_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun when_no_school_attached() {
        val session = getSession(schoolRef = "")

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(MESSAGES.SCHOOL_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun when_no_teachers_attached() {
        val session = getSession(teachersRef = emptyList())

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }
        assertThat(thrown.message, `is`(MESSAGES.TEACHERS_NOT_ASSOCIATED.message))
    }

    @Test
    internal fun session_already_exits() {
        val session = getSession()

        coEvery { mockedAppData.verifySession(session) } returns true


        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session = session)
            }
        }

        assertThat(thrown.message, `is`(MESSAGES.SESSION_ALREADY_EXISTS.message))
    }

    @Test
    internal fun session_within_same_date_window_is_found() {
        val session = getSession(
            startDate = parseISODate("2020-01-15"),
            endDate = parseISODate("2020-01-20"),
            startTime = LocalTime.NOON,
            endTime = LocalTime.NOON.plusHours(1)
        )

        coEvery { mockedAppData.verifySession(session) } returns false

        coEvery {
            mockedAppData.fetchSessions(
                adminRef = ADMIN_REF,
                schoolRef = "schoolRef",
                teacherRefs = listOf("teacherRef"),
                startDate = session.startDate
            )
        } returns listOf(
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

        val thrown = assertThrows(AppCoreException::class.java) {
            runBlocking {
                instance.saveSession(session = session)
            }
        }

        assertThat(thrown.message, `is`(MESSAGES.SESSIONS_CONFLICTS_EXISTS.message))
    }

    @Test
    internal fun session_within_same_date_different_window() {
        runBlocking {
            val session = getSession(
                startDate = parseISODate("2020-01-15"),
                endDate = parseISODate("2020-01-20"),
                startTime = LocalTime.NOON,
                endTime = LocalTime.NOON.plusHours(1)
            )

            coEvery { mockedAppData.verifySession(session) } returns false

            coEvery {
                mockedAppData.fetchSessions(
                    adminRef = ADMIN_REF,
                    schoolRef = "schoolRef",
                    teacherRefs = listOf("teacherRef"),
                    startDate = session.startDate
                )
            } returns listOf(
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
            coEvery { mockedAppData.saveSession(session) } returns session

            instance.saveSession(session = session)

            coVerify {
                mockedAppData.saveSession(session)
            }
        }
    }

    @Test
    internal fun session_groups_having_active_session_for_today() {
        runBlocking {
            coEvery {
                mockedAppData.fetchActiveSessions(
                    adminRef = ADMIN_REF
                )
            } returns listOf(
                // Active on all days
                getSession(weekDaysInfo = (0..6).map { true }),
                // Not active on any day -- Why are you here ??
                getSession(weekDaysInfo = (0..6).map { false })
            )
            coEvery {
                mockedAppData.fetchFutureSessions(
                    adminRef = ADMIN_REF
                )
            } returns listOf(getSession())

            coEvery {
                mockedAppData.fetchPastSessions(
                    adminRef = ADMIN_REF
                )
            } returns listOf(getSession())

            val sessionsMap: Map<String, List<Session>> = instance.allSessions(adminRef = ADMIN_REF)
            val todaySessions = sessionsMap.get("today")
            val pastSessions = sessionsMap.get("past")
            val futureSessions = sessionsMap.get("future")
            val totalSize = todaySessions!!.size + pastSessions!!.size + futureSessions!!.size
            assertThat(totalSize, `is`(3))

            val grouping = listOf<Session>(
                getSession(schoolRef = "School 1"),
                getSession(schoolRef = "School 1"),
                getSession(schoolRef = "School 2")
            ).groupBy { session -> session.schoolRef }
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
            coEvery {
                mockedAppData.fetchSessionsTill(
                    adminRef = "adminRef",
                    tillDate = today
                )
            } returns allSession

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

            coEvery {
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today
                )
            } returns allAttendance


            coEvery {
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today.minusDays(2)
                )
            } returns listOf(
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

            coEvery {
                mockedAppData.fetchAttendanceFor(
                    adminRef = "adminRef",
                    tillDate = today.minusDays(1)
                )
            } returns emptyList()

            val fetchAttendanceForADay =
                instance.fetchAttendance(adminRef = "adminRef", fromDate = today, toDate = today)
            fetchAttendanceForADay

            val fetchAttendanceForADuration =
                instance.fetchAttendance(adminRef = "adminRef", fromDate = today.minusDays(2), toDate = today)
            fetchAttendanceForADuration

        }
    }

    @Test
    internal fun admin_login_with_passcode_encryption() {
        runBlocking {
            val sixDigitPassCode = "123456"
            val admin = Admin()
            coEvery {
                mockedAppData.getAdminInfo("mobileNumber", "e10adc3949ba59abbe56e057f20f883e")
            } returns admin

            var loggedInUser: LoggedInUser = instance.login("mobileNumber", sixDigitPassCode)

            coVerify {
                mockedAppData.getAdminInfo("mobileNumber", "e10adc3949ba59abbe56e057f20f883e")
            }

            assertThat(loggedInUser.adminInfo, `is`(admin))
        }
    }

    @Test
    internal fun teacher_login_with_passcode_encryption() {
        runBlocking {
            val sixDigitPassCode = "678912"
            val teacher = Teacher(adminRef = "adminRef")

            coEvery {
                mockedAppData.getAdminInfo(any(), any())
            } returns null

            coEvery {
                mockedAppData.getTeacherInfo("mobileNumber", "73741a8c767a4db2d0ff6c208aa116ad")
            } returns teacher

            var loggedInUser: LoggedInUser = instance.login("mobileNumber", sixDigitPassCode)

            coVerify {
                mockedAppData.getTeacherInfo("mobileNumber", "73741a8c767a4db2d0ff6c208aa116ad")
            }

            assertThat(loggedInUser.teacherInfo, `is`(teacher))
        }
    }

    @Test
    internal fun dataholder_mapper_test() {
        val allWeekDays = listOf(
            DataHolder("1", "Monday"),
            DataHolder("2", "Tuesday"),
            DataHolder("3", "Wednesday"),
            DataHolder("4", "Thursday"),
            DataHolder("5", "Friday"),
            DataHolder("6", "Saturday"),
            DataHolder("7", "Sunday")
        )

        val selectedValues = allWeekDays.map { holder -> holder.selected }
        println(selectedValues)
    }

    @Test
    internal fun check_in_when_session_already_checked_In() {
        runBlocking {
            loginAsTeacher()

            val sessionToCheckin = getSession()
            coEvery {
                mockedAppData.findAttendanceForSession(sessionToCheckin)
            } returns getAttendance()


            val thrown = assertThrows(AppCoreWarnException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = sessionToCheckin,
                        teacherCurrentLocation = teacherCurrentLocation,
                        schoolLocation = schoolLocation
                    )
                }
            }
            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_ALREADY_CHECKED_IN.message))
        }
    }

    @Test
    internal fun check_in_when_session_already_checked_in_with_previous_successful_checkin() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef")

            coEvery {
                mockedAppData.checkedInAttendance(TEACHER_REF, sessionToCheckin)
            } returns getAttendance()

            instance.checkinToSession(
                session = sessionToCheckin,
                teacherCurrentLocation = teacherCurrentLocation,
                schoolLocation = schoolLocation
            )

            val thrown = assertThrows(AppCoreWarnException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = sessionToCheckin,
                        teacherCurrentLocation = teacherCurrentLocation,
                        schoolLocation = schoolLocation
                    )
                }
            }
            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_ALREADY_CHECKED_IN.message))
        }
    }

    @Test
    internal fun check_in_when_location_perimeter_does_not_meet() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef")

            every { teacherCurrentLocation.distanceTo(schoolLocation) } returns 51f

            val thrown = assertThrows(AppCoreException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = sessionToCheckin,
                        schoolLocation = schoolLocation,
                        teacherCurrentLocation = teacherCurrentLocation
                    )
                }
            }
            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_LOCATION_IS_TO_FAR.message))
        }
    }

    @Test
    internal fun check_in_when_session_checkin_time_past_specified_threshold() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef", startTime = LocalTime.now().minusMinutes(20))

            val thrown = assertThrows(AppCoreException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = sessionToCheckin,
                        schoolLocation = schoolLocation,
                        teacherCurrentLocation = teacherCurrentLocation
                    )
                }
            }

            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_TIME_MISMATCH_WITH_THRESHOLD.message))
        }
    }

    @Test
    internal fun check_in_at_earlier_time_then_the_session_time() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef", startTime = LocalTime.now().plusMinutes(20))

            val thrown = assertThrows(AppCoreException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = sessionToCheckin,
                        schoolLocation = schoolLocation,
                        teacherCurrentLocation = teacherCurrentLocation
                    )
                }
            }

            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_TIME_MISMATCH_WITH_THRESHOLD.message))
        }
    }

    @Test
    internal fun checkin_to_different_session_with_previous_session_checked_in() {
        runBlocking {
            loginAsTeacher()
            val firstSession = getSession(id = "sessionRef1")
            val firstAttendance = getAttendance(id = ATTENDANCE_REF, sessionRef = firstSession.id!!)

            coEvery {
                mockedAppData.checkedInAttendance(TEACHER_REF, firstSession)
            } returns firstAttendance

            instance.checkinToSession(
                session = firstSession,
                teacherCurrentLocation = teacherCurrentLocation,
                schoolLocation = schoolLocation
            )

            val secondSession = getSession(id = "sessionRef2")

            coEvery {
                mockedAppData.fetchSession(firstSession.id!!)
            } returns firstSession

            coEvery {
                mockedAppData.findAttendanceForSession(firstSession)
            } returns firstAttendance

            coEvery {
                mockedAppData.checkedOutAttendance(ATTENDANCE_REF)
            } returns mockk()

            coEvery {
                mockedAppData.checkedInAttendance(TEACHER_REF, secondSession)
            } returns getAttendance()

            val thrown = assertThrows(AppCoreWarnException::class.java) {
                runBlocking {
                    instance.checkinToSession(
                        session = secondSession,
                        teacherCurrentLocation = teacherCurrentLocation,
                        schoolLocation = schoolLocation
                    )
                }
            }
            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_PREVIOUS_FORCED_CHECKOUT.message))
            coVerify {
                mockedAppData.checkedInAttendance(TEACHER_REF, secondSession)
            }
            coVerify {
                mockedAppData.checkedOutAttendance(ATTENDANCE_REF)
            }
        }
    }


    @Test
    internal fun checkout_for_non_checked_in_session() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef", startTime = LocalTime.now().plusMinutes(20))

            val thrown = assertThrows(AppCoreWarnException::class.java) {
                runBlocking {
                    instance.checkoutFromSession(
                        session = sessionToCheckin
                    )
                }
            }

            assertThat(thrown.message, `is`(MESSAGES.TEACHER_SESSION_NO_SESSION_CHECKED_IN.message))
        }
    }

    @Test
    internal fun checkout_for_checked_in_session() {
        runBlocking {
            loginAsTeacher()
            val sessionToCheckin = getSession(id = "sessionRef", startTime = LocalTime.now().plusMinutes(20))

            coEvery {
                mockedAppData.findAttendanceForSession(sessionToCheckin)
            } returns getAttendance(id = ATTENDANCE_REF)

            coEvery {
                mockedAppData.checkedOutAttendance(ATTENDANCE_REF)
            } returns getAttendance()

            instance.checkoutFromSession(
                session = sessionToCheckin
            )

            coVerify {
                mockedAppData.checkedOutAttendance(ATTENDANCE_REF)
            }
        }
    }

    private suspend fun loginAsTeacher() {
        coEvery {
            mockedAppData.getAdminInfo(
                "11111111",
                "7fa8282ad93047a4d6fe6111c93b308a"
            )
        } returns null

        coEvery {
            mockedAppData.getTeacherInfo(
                "11111111",
                "7fa8282ad93047a4d6fe6111c93b308a"
            )
        } returns Teacher(id = TEACHER_REF, adminRef = ADMIN_REF)

        instance.login("11111111", "1111111")
    }

    companion object {
        private val ADMIN_REF: String = "fw7aJ1dVDpQndyHFhDsv"
        private val TEACHER_REF: String = "fw7aJ1dVDpQndyHFhDsv"
        private val ATTENDANCE_REF: String = "fw7aJ1dVDpQndyHFhDsv"
    }
}
