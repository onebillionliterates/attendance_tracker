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

/**
// FiReBasE locally -
// https://firebase.google.com/docs/emulator-suite
// https://firebase.google.com/docs/emulator-suite/connect_and_prototype
val db = Firebase.firestore
db.firestoreSettings = firestoreSettings {
host = "10.0.2.2:8080"
isSslEnabled = false
isPersistenceEnabled = false
}**/
@MediumTest
class AppDataTest {
    @Before
    fun setupClass() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        AndroidThreeTen.init(appContext);
        Firebase.initialize(appContext)
    }

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
