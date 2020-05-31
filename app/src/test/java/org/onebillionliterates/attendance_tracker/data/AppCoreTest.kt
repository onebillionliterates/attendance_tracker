package org.onebillionliterates.attendance_tracker.data

import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Ignore
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalTime
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE

class AppCoreTest {
    private val mockedAppData: AppData = Mockito.mock(AppData::class.java)
    private val instance = AppCore(mockedAppData)
    private fun getSession(
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
            Mockito.`when`(mockedAppData.verifySession(session)).thenReturn(true)
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
            Mockito.`when`(
                mockedAppData.fetchSessions(
                    adminRef = "adminRef",
                    schoolRef = "schoolRef",
                    teacherRefs = listOf("teacherRef"),
                    startDate = session.startDate
                )
            ).thenReturn(
                listOf(
                    getSession(
                        startDate = parseISODate("2020-01-01"),
                        endDate = parseISODate("2020-01-31"),
                        weekDaysInfo = listOf(true, false, true, false, false, false, false),
                        startTime = LocalTime.NOON,
                        endTime = LocalTime.NOON.plusMinutes(30)
                    ),
                    getSession(
                        startDate = parseISODate("2020-01-10"),
                        endDate = parseISODate("2020-01-19"),
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = LocalTime.NOON.plusMinutes(30),
                        endTime = LocalTime.NOON.plusHours(1)
                    ),
                    getSession(
                        startDate = parseISODate("2020-01-16"),
                        endDate = parseISODate("2020-01-19"),
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = LocalTime.NOON.plusMinutes(15),
                        endTime = LocalTime.NOON.minusMinutes(15)
                    ),
                    getSession(
                        startDate = parseISODate("2020-01-20"),
                        endDate = parseISODate("2020-01-20"),
                        weekDaysInfo = listOf(false, false, true, false, false, false, false),
                        startTime = LocalTime.NOON.minusMinutes(15),
                        endTime = LocalTime.NOON.plusMinutes(15)
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
            Mockito.`when`(
                mockedAppData.fetchSessions(
                    adminRef = "adminRef",
                    schoolRef = "schoolRef",
                    teacherRefs = listOf("teacherRef"),
                    startDate = session.startDate
                )
            ).thenReturn(emptyList())
        }
        runBlocking {
            instance.saveSession(session = session)
            Mockito.verify(mockedAppData).saveSession(session)
        }
    }
}
