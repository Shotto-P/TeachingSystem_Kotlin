package au.edu.utas.shotto.markingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import au.edu.utas.shotto.markingsystem.databinding.ActivityAddCourseBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

const val COURSEID_KEY : String = "COURSEID"
const val COURSENAME_KEY : String = "COURSENAME"

class AddCourse : AppCompatActivity() {
    private lateinit var ui : ActivityAddCourseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddCourseBinding.inflate(layoutInflater)
        setContentView(ui.root)

        ui.btnAddCourse.setOnClickListener {
            var enteredCourseId = ui.txtCourseId.text.toString().toUpperCase()
            //remove whitespace fromt the string
            enteredCourseId = enteredCourseId.replace("\\s".toRegex(), "")
            val enteredCourseName = ui.txtCourseName.text.toString()

            val db = Firebase.firestore
            var coursesCollection = db.collection("courses")

            //check if the new course exists or not in the db
            coursesCollection.document(enteredCourseId)
                .update("id", enteredCourseId)
                .addOnSuccessListener {
                    Log.d("TAG", "document exists.")
                    Toast.makeText(this, "Course already exist!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener {
                    //if update fails-->document not exists-->toast a message
                    Log.d("TAG", "course does not exist.")

                    var newCourse = Course(
                        id = enteredCourseId,
                        name = enteredCourseName
                    )
                    /*add the course info entered by the user to the database*/
                    coursesCollection
                        .document(newCourse.id).set(newCourse)
                        .addOnSuccessListener {
                            Log.d(FIREBASE_TAG, "Document created.")

                        }
                        .addOnFailureListener {
                            Log.e(FIREBASE_TAG, "Error writing document", it)
                        }

                }

            val i = Intent(this, MainActivity::class.java)
            startActivity(i)
        }
    }
}