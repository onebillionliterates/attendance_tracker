package org.onebillionliterates.attendance_tracker.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId


class AppData() {
    private val db = Firebase.firestore
    private val TAG: String = "APP_DATA"

    fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()) =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone)

    suspend fun getAdminInfo(mobileNumber: String, passCode: String): Admin {
        val data = db.collection("admin")
            .whereEqualTo("mobileNumber", mobileNumber)
            .whereEqualTo("passCode", passCode)
            .get()
            .await();

        return data.documents.map { document ->
            Admin(
                document.id,
                document.getString("mobileNumber"),
                document.getString("name"),
                document.getString("passCode"),
                document.getString("remarks"),
                document.getTimestamp("createdAt")?.toLocalDateTime()
            )
        }.first()
    }
}
