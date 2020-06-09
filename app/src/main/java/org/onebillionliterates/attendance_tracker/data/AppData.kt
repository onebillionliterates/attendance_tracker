package org.onebillionliterates.attendance_tracker.data

import android.location.Location
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import org.threeten.bp.*


class AppData {
    private val TAG: String = "APP_DATA"
    private val db = Firebase.firestore
    private val adminCollection = db.collection("admin")
    private val teacherCollection = db.collection("teachers")
    private val schoolCollection = db.collection("schools")
    private val sessionsCollection = db.collection("sessions")
    private val attendanceCollection = db.collection("attendance")

    private object HOLDER {
        val INSTANCE = AppData()
    }

    companion object {
        val instance: AppData by lazy { HOLDER.INSTANCE }
    }

    fun Timestamp.toLocalDateTime(zone: ZoneId = ZoneOffset.UTC) =
        LocalDateTime.ofInstant(Instant.ofEpochMilli(seconds * 1000 + nanoseconds / 1000000), zone)

    fun Timestamp.toLocalDate(zone: ZoneId = ZoneId.systemDefault()) = this.toLocalDateTime().toLocalDate()

    fun LocalDateTime.toTimestamp(zone: ZoneId = ZoneOffset.UTC) =
        Timestamp(toEpochSecond(ZoneOffset.of(zone.id)), nano)

    fun LocalDate.toTimestamp(zone: ZoneId = ZoneOffset.UTC) = this.atStartOfDay().toTimestamp(zone)


    fun geoPointToLocation(geoPoint: GeoPoint): Location {
        val location = Location("FIRESTORE")
        location.latitude = geoPoint.latitude
        location.longitude = geoPoint.longitude
        return location
    }

    suspend fun getAdminInfo(mobileNumber: String, passCode: String): Admin? {
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
        }.firstOrNull()
    }

    suspend fun saveTeacher(teacherToSave: Teacher): Teacher {
        val createdAt = LocalDateTime.now(ZoneOffset.UTC)
        val data: DocumentReference = teacherCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(teacherToSave.adminRef),
                    "mobileNumber" to teacherToSave.mobileNumber,
                    "name" to teacherToSave.name,
                    "passCode" to teacherToSave.passCode,
                    "remarks" to teacherToSave.remarks,
                    "createdAt" to createdAt.toTimestamp(),
                    "emailId" to teacherToSave.emailId,
                    "verificationId" to teacherToSave.verificationId
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
            createdAt = createdAt,
            emailId = teacherToSave.emailId,
            verificationId = teacherToSave.verificationId

        )
    }

    fun updateTeacher(teacherToSave: Teacher): Teacher {
        val createdAt = LocalDateTime.now(ZoneOffset.UTC)
        if (teacherToSave.id != null) {
            teacherCollection.document(teacherToSave.id!!)
                .set(
                    hashMapOf(
                        "adminRef" to adminCollection.document(teacherToSave.adminRef),
                        "mobileNumber" to teacherToSave.mobileNumber,
                        "name" to teacherToSave.name,
                        "passCode" to teacherToSave.passCode,
                        "remarks" to teacherToSave.remarks,
                        "createdAt" to createdAt.toTimestamp(),
                        "emailId" to teacherToSave.emailId,
                        "verificationId" to teacherToSave.verificationId
                    )
                )
        }
        return teacherToSave
    }

    suspend fun getTeacherInfo(mobileNumber: String, passCode: String): Teacher? {
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
                document.getTimestamp("createdAt")?.toLocalDateTime(),
                document.getString("emailId"),
                document.getString("verificationId")
            )
        }.firstOrNull()
    }

    suspend fun getTeachersCollection(): MutableList<Teacher> {

        val teachersList: MutableList<Teacher> = ArrayList()

        teacherCollection
            .get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    teachersList.add(
                        Teacher(
                            document.id,
                            document.getDocumentReference("adminRef")!!.id,
                            document.getString("mobileNumber"),
                            document.getString("name"),
                            document.getString("passCode"),
                            document.getString("remarks"),
                            document.getTimestamp("createdAt")?.toLocalDateTime(),
                            document.getString("emailId"),
                            document.getString("verificationId")
                        )
                    )
                    Log.d(TAG, "${document.id} => ${document.data}")
                }
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
            }.await()
        return teachersList
    }

    suspend fun fetchTeachers(adminRef: String): List<Teacher> {
        return teacherCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .get()
            .await()
            .map { document ->
                Teacher(
                    document.id,
                    document.getDocumentReference("adminRef")!!.id,
                    document.getString("mobileNumber"),
                    document.getString("name"),
                    document.getString("passCode"),
                    document.getString("remarks"),
                    document.getTimestamp("createdAt")?.toLocalDateTime(),
                    document.getString("emailId"),
                    document.getString("verificationId")
                )
            }
    }

    suspend fun fetchSchools(adminRef: String): List<School> {
        return schoolCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .get()
            .await()
            .map { document ->
                School(
                    id = document.id,
                    adminRef = document.getDocumentReference("adminRef")!!.id,
                    name = document.getString("name"),
                    uniqueCode = document.getString("uniqueCode"),
                    remarks = document.getString("remarks"),
                    createdAt = document.getTimestamp("createdAt")?.toLocalDateTime(),
                    location = geoPointToLocation(document.getGeoPoint("location")!!)
                )
            }
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
                    "teachersRef" to sessionToSave.teacherRefs.map { ref -> teacherCollection.document(ref) },
                    "startDate" to sessionToSave.startDate.toTimestamp(),
                    "endDate" to sessionToSave.endDate.toTimestamp(),
                    "startTime" to sessionToSave.startTime.toNanoOfDay(),
                    "endTime" to sessionToSave.endTime.toNanoOfDay(),
                    "weekDaysInfo" to sessionToSave.weekDaysInfo
                )
            )
            .await()

        return Session(
            id = data.id,
            adminRef = sessionToSave.adminRef,
            schoolRef = sessionToSave.schoolRef,
            teacherRefs = sessionToSave.teacherRefs,
            startDate = sessionToSave.startDate,
            endDate = sessionToSave.endDate,
            startTime = sessionToSave.startTime,
            endTime = sessionToSave.endTime,
            weekDaysInfo = sessionToSave.weekDaysInfo
        )
    }

    suspend fun fetchSessions(
        adminRef: String,
        schoolRef: String,
        teacherRefs: List<String>,
        startDate: LocalDate
    ): List<Session> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereEqualTo("schoolRef", schoolCollection.document(schoolRef))
            .whereArrayContainsAny("teachersTef", teacherRefs.map { teacherCollection.document() })
            .whereGreaterThanOrEqualTo("endDate", startDate.toTimestamp())
            .get()
            .await()

        return mapSessions(data)
    }

    suspend fun fetchActiveSessions(
        adminRef: String
    ): List<Session> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereGreaterThanOrEqualTo("endDate", LocalDate.now().toTimestamp())
            .get()
            .await()

        return mapSessions(data)
    }

    suspend fun fetchPastSessions(
        adminRef: String
    ): List<Session> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereLessThan("endDate", LocalDate.now().toTimestamp())
            .get()
            .await()

        return mapSessions(data)
    }

    suspend fun fetchFutureSessions(
        adminRef: String
    ): List<Session> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereGreaterThan("startDate", LocalDate.now().toTimestamp())
            .get()
            .await()

        return mapSessions(data)
    }

    private fun mapSessions(data: QuerySnapshot): List<Session> {
        return data.documents.map { document ->
            Session(
                id = document.id,
                adminRef = document.getDocumentReference("adminRef")!!.id,
                schoolRef = document.getDocumentReference("schoolRef")!!.id,
                teacherRefs = listOfTeacherRefs(document),
                startDate = document.getTimestamp("startDate")?.toLocalDate()!!,
                endDate = document.getTimestamp("endDate")?.toLocalDate()!!,
                startTime = LocalTime.ofNanoOfDay(document.get("startTime") as Long),
                endTime = LocalTime.ofNanoOfDay(document.get("endTime") as Long),
                weekDaysInfo = listOfWeekDays(document)
            )
        }
    }

    suspend fun verifySession(session: Session): Boolean {

        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(session.adminRef))
            .whereEqualTo("schoolRef", schoolCollection.document(session.schoolRef))
            .whereArrayContainsAny("teachersTef", session.teacherRefs.map { teacherCollection.document() })
            .whereEqualTo("startDate", session.startDate.toTimestamp())
            .whereEqualTo("endDate", session.endDate.toTimestamp())
            .whereEqualTo("startTime", session.startTime)
            .whereEqualTo("endTime", session.endTime)
            .get()
            .await()

        return data.documents.size > 0
    }

    private fun listOfWeekDays(document: DocumentSnapshot): List<Boolean> =
        (document.get("weekDaysInfo") as List<*>).map { value ->
            value as Boolean
        }


    private fun listOfTeacherRefs(document: DocumentSnapshot) =
        (document.get("teachersRef") as List<*>).map { doc -> doc as DocumentReference }
            .map { teacherDoc -> teacherDoc.id }


    suspend fun saveAttendance(attendanceToSave: Attendance): Attendance {
        val data: DocumentReference = attendanceCollection
            .add(
                hashMapOf(
                    "adminRef" to adminCollection.document(attendanceToSave.adminRef),
                    "sessionRef" to sessionsCollection.document(attendanceToSave.sessionRef),
                    "teacherRef" to teacherCollection.document(attendanceToSave.teacherRef),
                    "schoolRef" to schoolCollection.document(attendanceToSave.schoolRef),
                    "inTime" to attendanceToSave.inTime.toNanoOfDay(),
                    "outTime" to attendanceToSave.outTime.toNanoOfDay(),
                    "createdAt" to attendanceToSave.createdAt.toTimestamp(),
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
            createdAt = attendanceToSave.createdAt,
            remarks = attendanceToSave.remarks
        )
    }

    suspend fun fetchSessionsTill(adminRef: String, tillDate: LocalDate): List<Session> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = sessionsCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereLessThanOrEqualTo("startDate", tillDate.toTimestamp())
            .get()
            .await()

        return mapSessions(data)
    }

    suspend fun fetchAttendanceFor(adminRef: String, tillDate: LocalDate): List<Attendance> {
        // Range Queries Are Not Allowed in FireStore - https://firebase.google.com/docs/firestore/query-data/queries
        val data = attendanceCollection
            .whereEqualTo("adminRef", adminCollection.document(adminRef))
            .whereEqualTo("createdAt", tillDate.toTimestamp())
            .get()
            .await()

        return return data.documents.map { document ->
            Attendance(
                id = document.id,
                adminRef = document.getDocumentReference("adminRef")!!.id,
                sessionRef = document.getDocumentReference("sessionRef")!!.id,
                schoolRef = document.getDocumentReference("schoolRef")!!.id,
                teacherRef = document.getDocumentReference("teacherRef")!!.id,
                createdAt = document.getTimestamp("createdAt")?.toLocalDate()!!,
                inTime = LocalTime.ofNanoOfDay(document.get("inTime") as Long),
                outTime = LocalTime.ofNanoOfDay(document.get("outTime") as Long)
            )
        }
    }

    suspend fun fetchTeachersForRefs(adminRef: String, teacherRefs: List<String>): List<Teacher> {
        val cleanedUpRefs = teacherRefs
            .map { ref -> ref.split("/")[2] }
        val data =
            cleanedUpRefs
                .map { ref -> teacherCollection.document(ref).get().await() }

        return data.map { document ->
            Teacher(
                document.id,
                document.getDocumentReference("adminRef")!!.id,
                document.getString("mobileNumber"),
                document.getString("name"),
                document.getString("passCode"),
                document.getString("remarks"),
                document.getTimestamp("createdAt")?.toLocalDateTime()
            )
        }
            .filter { teacher ->
                teacher.adminRef == adminRef
            }
    }
}
