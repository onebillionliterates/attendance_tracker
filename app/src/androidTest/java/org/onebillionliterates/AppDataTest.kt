package org.onebillionliterates

import android.location.Location
import android.util.Log
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import com.jakewharton.threetenabp.AndroidThreeTen
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
        AndroidThreeTen.init(appContext);
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

    /*@Test
    fun get_admin_info() {
        runBlocking {
            val TAG = "TESTING APP COROUTINE"
            val appData = AppData();
            val mobileNumber = "+15555215554"
            val passcode = "e10adc3949ba59abbe56e057f20f883e"
            var admin = appData.getAdminInfo(mobileNumber, passcode)

            assertThat(admin, Is(notNullValue()))
            Log.d(TAG, "PARTICULAR => $admin")
        }
    }

    @Test
    fun get_teachers_in() {
        runBlocking {
            val TAG = "TESTING APP COROUTINE"
            val appData = AppData();
                var teachersForIds = appData.fetchTeachersForRefs(adminRef = "fw7aJ1dVDpQndyHFhDsv", teacherRefs = listOf<String>(
                "/teachers/MPw0TDVnXFmX64vEHZAJ"
            ))

            assertThat(teachersForIds, Is(notNullValue()))
            Log.d(TAG, "PARTICULAR => $teachersForIds")
        }
    }*/


    // https://www.google.com/maps/place/Delhi+Public+School,+Bangalore+East/@12.8844321,77.7232494,17z/data=!3m1!4b1!4m8!1m2!2m1!1sDPS!3m4!1s0x0:0xe7851645e27bf86c!8m2!3d12.8844266!4d77.7254391
    // https://www.google.com/maps/place/KIDS+PLAY+AREA+BIRTHDAY+VENUE+FIELD+TRIP+%7C+KIDZTOPIAA+IN+SARJAPUR+ROAD/@12.8868623,77.7199532,15z/data=!4m8!1m2!2m1!1sDPS!3m4!1s0x3bae138de4edd285:0xf575953dad24146d!8m2!3d12.8946576!4d77.7246505
    @Test
    fun location_distance_verification() {
        runBlocking {
            val TAG = "TESTING LOCATION"
            val schoolLocation = Location("SAVED_LOCATION")
            schoolLocation.latitude = 12.8844321
            schoolLocation.longitude = 77.7232494

            val teacherCurrentLocation = Location("GPS_LOCATION")
            teacherCurrentLocation.latitude = 12.8868623
            teacherCurrentLocation.longitude = 77.7199532

            val distanceBetween = teacherCurrentLocation.distanceTo(schoolLocation)
            Log.d(TAG, "PARTICULAR => $distanceBetween")
        }
    }

}
