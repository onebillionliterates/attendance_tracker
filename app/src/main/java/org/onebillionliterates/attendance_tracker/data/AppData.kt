package org.onebillionliterates.attendance_tracker.data

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset


class AppData() {
    private val db = Firebase.firestore
    private val TAG: String = "APP_DATA"
    private val adminCollection = db.collection("admin")
    private val teacherCollection = db.collection("teachers")

    fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()) =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone)

    fun LocalDateTime.toTimestamp(zone: ZoneId = ZoneOffset.UTC) = Timestamp(toEpochSecond(ZoneOffset.of(zone.id)), nano)

    suspend fun getAdminInfo(mobileNumber: String, passCode: String): Admin {
        val data = adminCollection
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

    suspend fun saveTeacher(teacherToSave: Teacher): Teacher {
        val data: DocumentReference = teacherCollection
            .add(
                hashMapOf(
                    "mobileNumber" to teacherToSave.mobileNumber,
                    "name" to teacherToSave.name,
                    "passCode" to teacherToSave.passCode,
                    "remarks" to teacherToSave.remarks,
                    "createdAt" to teacherToSave.createdAt?.toTimestamp()
                )
            )
            .await()

        return Teacher(data.id, teacherToSave.mobileNumber, teacherToSave.name, teacherToSave.passCode, teacherToSave.remarks, teacherToSave.createdAt)
    }

    suspend fun getTeacherInfo(mobileNumber: String, passCode: String): Teacher {
        val data = teacherCollection
            .whereEqualTo("mobileNumber", mobileNumber)
            .whereEqualTo("passCode", passCode)
            .get()
            .await();

        return data.documents.map { document ->
            Teacher(
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
