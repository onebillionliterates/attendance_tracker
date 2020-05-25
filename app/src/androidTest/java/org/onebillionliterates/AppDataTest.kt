package org.onebillionliterates

import android.util.Log
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.onebillionliterates.attendance_tracker.data.AppData


@MediumTest
class AppDataTest {
    @Before
    fun setupClass() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        Firebase.initialize(appContext)
        /**
        // FiReBasE locally -
        // https://firebase.google.com/docs/emulator-suite
        // https://firebase.google.com/docs/emulator-suite/connect_and_prototype
        val db = Firebase.firestore
        db.firestoreSettings = firestoreSettings {
        host = "10.0.2.2:8080"
        isSslEnabled = false
        isPersistenceEnabled = false
        }
         **/
    }

    @Test
    fun instrumentation_test() {
        val TAG = "INSTRUMENTATION TESTS"
        Log.d(TAG, "THIS IS SAMPLE TEST")
    }

    @Test
    fun instance_test() {
        val appDataTest = AppData();
        assertNotNull(appDataTest)
    }

    @Test
    fun get_admin_info() {
        runBlocking {
            val TAG = "TESTING APP COROUTINE"
            val appData = AppData();
            val mobileNumber = "8884410287"
            val passcode = "337703"
            var admin = appData.getAdminInfo(mobileNumber, passcode)

            assertThat(admin, Is(notNullValue()))
            Log.d(TAG, "PARTICULAR => $admin")
        }
    }

}
