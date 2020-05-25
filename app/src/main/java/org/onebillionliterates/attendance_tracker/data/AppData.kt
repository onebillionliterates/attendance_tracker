package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.threeten.bp.*


class AppData() {
    private val db = Firebase.firestore
    private val TAG: String = "APP_DATA"
    private val adminCollection = db.collection("admin")
    private val teacherCollection = db.collection("teachers")
    private val schoolCollection = db.collection("schools")
    private val sessionsCollection = db.collection("sessions")
    private val attendanceCollection = db.collection("attendance")

    fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneId.systemDefault()) =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone)

    fun LocalDateTime.toTimestamp(zone: ZoneId = ZoneOffset.UTC) =
        Timestamp(toEpochSecond(ZoneOffset.of(zone.id)), nano)


    fun geoPointToLocation(geoPoint: GeoPoint): Location {
        val location = Location("FIRESTORE")
        location.latitude = geoPoint.latitude
        location.longitude = geoPoint.longitude
        return location
    }

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
        val createdAt = LocalDateTime.now()
        val data: DocumentReference = teacherCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(teacherToSave.adminRef),
                    "mobileNumber" to teacherToSave.mobileNumber,
                    "name" to teacherToSave.name,
                    "passCode" to teacherToSave.passCode,
                    "remarks" to teacherToSave.remarks,
                    "createdAt" to createdAt.toTimestamp()
                )
            )
            .await()

        return Teacher(
            id = data.id,
            adminRef = teacherToSave.adminRef,
            mobileNumber = teacherToSave.mobileNumber,
            name = teacherToSave.name,
            passCode = teacherToSave.passCode,
            remarks = teacherToSave.remarks,
            createdAt = createdAt
        )
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
                document.getDocumentReference("adminRef")!!.id,
                document.getString("mobileNumber"),
                document.getString("name"),
                document.getString("passCode"),
                document.getString("remarks"),
                document.getTimestamp("createdAt")?.toLocalDateTime()
            )
        }.first()
    }

    suspend fun saveSchool(schoolToSave: School): School {

        val createdAt = LocalDateTime.now()
        val data: DocumentReference = schoolCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(schoolToSave.adminRef),
                    "name" to schoolToSave.name,
                    "uniqueCode" to schoolToSave.uniqueCode,
                    "remarks" to schoolToSave.remarks,
                    "location" to GeoPoint(schoolToSave.location!!.latitude, schoolToSave.location.longitude),
                    "createdAt" to createdAt.toTimestamp()
                )
            )
            .await()

        return School(
            id = data.id,
            adminRef = schoolToSave.adminRef,
            name = schoolToSave.name,
            uniqueCode = schoolToSave.uniqueCode,
            remarks = schoolToSave.remarks,
            location = schoolToSave.location,
            createdAt = createdAt
        )
    }

    suspend fun getSchoolInfo(adminRef: String, name: String, uniqueCode: String): School {
        val data = schoolCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereEqualTo("name", name)
            .whereEqualTo("uniqueCode", uniqueCode)
            .get()
            .await();

        return data.documents.map { document ->
            School(
                id = document.id,
                adminRef = document.getDocumentReference("adminRef")!!.id,
                name = document.getString("name"),
                uniqueCode = document.getString("uniqueCode"),
                remarks = document.getString("remarks"),
                createdAt = document.getTimestamp("createdAt")?.toLocalDateTime(),
                location = geoPointToLocation(document.getGeoPoint("location")!!)
            )
        }.first()
    }

    suspend fun saveSession(sessionToSave: Session): Session {
        val data: DocumentReference = sessionsCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(sessionToSave.adminRef),
                    "schoolRef" to schoolCollection.document(sessionToSave.schoolRef),
                    "teacherRef" to teacherCollection.document(sessionToSave.teacherRef),
                    "startDate" to sessionToSave.startDate.toTimestamp(),
                    "endDate" to sessionToSave.endDate.toTimestamp(),
                    "durationInSecs" to sessionToSave.durationInSecs,
                    "mondayWorking" to sessionToSave.mondayWorking,
                    "tuesdayWorking" to sessionToSave.tuesdayWorking,
                    "wednesdayWorking" to sessionToSave.wednesdayWorking,
                    "thursdayWorking" to sessionToSave.thursdayWorking,
                    "fridayWorking" to sessionToSave.fridayWorking,
                    "saturdayWorking" to sessionToSave.saturdayWorking,
                    "sundayWorking" to sessionToSave.sundayWorking
                )
            )
            .await()

        return Session(
            id = data.id,
            adminRef = sessionToSave.adminRef,
            schoolRef = sessionToSave.schoolRef,
            teacherRef = sessionToSave.teacherRef,
            startDate = sessionToSave.startDate,
            endDate = sessionToSave.endDate,
            durationInSecs = sessionToSave.durationInSecs,
            mondayWorking = sessionToSave.mondayWorking,
            tuesdayWorking = sessionToSave.tuesdayWorking,
            wednesdayWorking = sessionToSave.wednesdayWorking,
            thursdayWorking = sessionToSave.thursdayWorking,
            fridayWorking = sessionToSave.fridayWorking,
            saturdayWorking = sessionToSave.saturdayWorking,
            sundayWorking = sessionToSave.sundayWorking
        )
    }


    suspend fun saveAttendance(attendanceToSave: Attendance): Attendance {
        val data: DocumentReference = attendanceCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(attendanceToSave.adminRef),
                    "sessionRef" to sessionsCollection.document(attendanceToSave.sessionRef),
                    "teacherRef" to teacherCollection.document(attendanceToSave.teacherRef),
                    "schoolRef" to schoolCollection.document(attendanceToSave.schoolRef),
                    "inTime" to attendanceToSave.inTime.toTimestamp(),
                    "outTime" to attendanceToSave.outTime.toTimestamp(),
                    "remarks" to attendanceToSave.remarks
                )
            )
            .await()

        return Attendance(
            id = data.id,
            adminRef = attendanceToSave.adminRef,
            schoolRef = attendanceToSave.schoolRef,
            teacherRef = attendanceToSave.teacherRef,
            sessionRef = attendanceToSave.sessionRef,
            inTime = attendanceToSave.inTime,
            outTime = attendanceToSave.outTime,
            remarks = attendanceToSave.remarks
        )
    }
}
