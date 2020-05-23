package org.onebillionliterates

import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.firestoreSettings
import com.google.firebase.ktx.Firebase
import com.google.firebase.ktx.initialize
import kotlinx.coroutines.runBlocking
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.collection.IsCollectionWithSize
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.onebillionliterates.attendance_tracker.data.AppData


@RunWith(AndroidJUnit4::class)
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
    fun instance_test() {
        val appDataTest = AppData();
        assertNotNull(appDataTest)
    }

    @Test
    fun get_admin_info() = runBlocking {
        val TAG = "TESTING APP COROUTINE"
        val appData = AppData();
        val mobileNumber = "8884410287"
        val passcode = 337703

        val data = appData.getAdminInfo(mobileNumber, passcode)

        assertThat(data, IsCollectionWithSize.hasSize(1))
        for (document in data) {
            Log.d(TAG, "PARTICULAR ${document.id} => ${document.data}")
        }
        println(data)
    }

    @Test
    fun get_all_admins() = runBlocking {
        val TAG = "TESTING APP COROUTINE"
        val appData = AppData();

        val data = appData.allAdmins()

        assertNotNull(data)
        for (document in data) {
            Log.d(TAG, "ALL_ADMINS ${document.id} => ${document.data}")
        }
        println(data)
    }
}
