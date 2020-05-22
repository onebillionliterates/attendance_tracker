package org.onebillionliterates.attendance_tracker.data

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class AppData {
    private val db = FirebaseFirestore.getInstance()
    private val TAG: String = "APP_DATA"

    fun getAdminInfo(mobileNumber: String, passcode: Int) {
        db.collection("admin")
            .whereEqualTo("mobile_number", mobileNumber)
            .whereEqualTo("passcode", passcode)
            .get()
            .addOnSuccessListener { result ->
                Log.w(TAG, "FOR PARTICULAR INFO " + result.size())
                for (document in result) {
                    Log.d(TAG, "PARTICULAR_ADMIN ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }

    fun allAdmins() {
        db.collection("admin")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    Log.d(TAG, "ALL_ADMINS ${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents.", exception)
            }
    }
}
