package org.onebillionliterates.attendance_tracker.util

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import org.onebillionliterates.attendance_tracker.data.Teacher
import org.threeten.bp.*

class DbUtils {

    companion object {

        //private lateinit var database: DatabaseReference
        var TAG = this::class.qualifiedName

        val db = FirebaseFirestore.getInstance()

//        fun writeNewTeacher(context: Context, mobileNumber: String, emailId: String, name: String,
//                            verificationId: String?) {
//
//            val db = FirebaseFirestore.getInstance()
//            val id = db.collection("teachers").document().id
//
//            val teacher = Teacher(
//                id,
//                "mani",
//                mobileNumber,
//                name,
//                "qwesdf",
//                "ermarkp with Id",
//                LocalDateTime.now(ZoneOffset.UTC),
//                emailId,
//                verificationId
//            )
//            Log.d(TAG,"Writing data into fire store with id $id")
//
//
//            db.collection("teachers").document(id).set(teacher)
//        }

        fun getTeachersCollection():MutableList<Teacher>{

            val teachersList: MutableList<Teacher> = ArrayList()

            db.collection("teachers")
                //.whereEqualTo("capital", true)
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents) {
                        teachersList.add(Teacher(
                            "id",//document.getString("id"),
                            document.getString("adminRef").toString(),
                            document.getString("mobileNumber"),
                            document.getString("name"),
                            document.getString("passCode"),
                            document.getString("remarks"),
                            LocalDateTime.now(),//chage this
                            document.getString("emailId"),
                            document.getString("verificationId")
                        ))
                        Log.d(TAG, "${document.id} => ${document.data}")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Error getting documents: ", exception)
                }

            return teachersList
        }
    }
}