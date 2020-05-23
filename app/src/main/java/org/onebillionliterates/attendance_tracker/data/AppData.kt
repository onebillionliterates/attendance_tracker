package org.onebillionliterates.attendance_tracker.data

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

class AppData() {
    private val db = Firebase.firestore
    private val TAG: String = "APP_DATA"

    suspend fun getAdminInfo(mobileNumber: String, passcode: Int): MutableList<DocumentSnapshot> {
        val data = db.collection("admin")
            .whereEqualTo("mobile_number", mobileNumber)
            .whereEqualTo("passcode", passcode)
            .get()
            .await();

        return data.documents
    }

    suspend fun allAdmins(): MutableList<DocumentSnapshot> {
        val data = db.collection("admin")
            .get()
            .await();

        return data.documents
    }
}
