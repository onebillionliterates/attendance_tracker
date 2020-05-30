package org.onebillionliterates.attendance_tracker.data

import com.google.firebase.Timestamp
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.threeten.bp.*
import org.threeten.bp.format.DateTimeFormatter.ISO_LOCAL_DATE

class AppCoreTest {
    private val mockedAppData: AppData = Mockito.mock(AppData::class.java)
    private val instance = AppCore(mockedAppData)
    private fun getSession(
        adminRef: String = "adminRef",
        schoolRef: String? = "schoolRef",
        teachersRef: List<String> = listOf("teacherRef"),
        startDate: LocalDateTime = LocalDateTime.now(),
        endDate: LocalDateTime = LocalDateTime.now(),
        durationInSecs: Long = 30 * 60,
        weekDaysInfo: List<Boolean> = listOf(true, false, true, false, true, false, false)
    ): Session {
        return Session(
            adminRef = adminRef,
            schoolRef = schoolRef!!,
            startDate = startDate,
            endDate = endDate,
            durationInSecs = durationInSecs,
            teacherRefs = teachersRef,
            weekDaysInfo = weekDaysInfo
        )
    }

    fun parseISODate(isoDate:String) = LocalDateTime.of(LocalDate.parse(isoDate, ISO_LOCAL_DATE), LocalTime.MIDNIGHT)

    @Test
    internal fun minimum_30_mins_session() {
        val session = getSession(durationInSecs = 0)

        val thrown = assertThrows(IllegalArgumentException::class.java) {
            runBlocking {
                instance.saveSession(session)
            }
        }

        assertThat(thrown.message, `is`(ERRORS.INVALID_DURATION.message))
    }

    @Test
    internal fun end_is_before_start() {
        val session = getSession(endDate = LocalDateTime.now().minusDays(1))

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
            endDate = parseISODate("2020-01-20")
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
                        weekDaysInfo = listOf(true, false, true, false, false, false, false)
                    ),

                    getSession(
                        startDate = parseISODate("2020-01-10"),
                        endDate = parseISODate("2020-01-19"),
                        weekDaysInfo = listOf(false, false, true, false, false, false, false)
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
}
