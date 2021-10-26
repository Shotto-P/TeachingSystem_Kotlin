package au.edu.utas.shotto.markingsystem

import android.content.Intent
import android.database.DataSetObserver
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.shotto.markingsystem.databinding.ActivityCourseHomepageBinding
import au.edu.utas.shotto.markingsystem.databinding.StudentListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
val studentItems = mutableListOf<Student>()
const val COURSEID : String = "COURSEID"
const val STUDENTID_KEY : String = "STUDENTID"
const val STUDENTNAME_KEY : String = "STUDENTNAME"

class CourseHomepage : AppCompatActivity() {
    private lateinit var ui : ActivityCourseHomepageBinding

    override fun onResume() {
        super.onResume()

        ui.StudentList.adapter?.notifyDataSetChanged()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityCourseHomepageBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val courseId = intent.getStringExtra(SELECTED_COURSE_KEY).toString()
        Log.d("TEST", courseId)
        ui.toolbar.title = courseId

        ui.BtnEditStudent.setOnClickListener {
            val intentToEditStudent = Intent(this, AddStudent::class.java)
            intentToEditStudent.putExtra(COURSEID, courseId)
            startActivity(intentToEditStudent)
        }

        val db = Firebase.firestore
        val coursesCollection = db.collection("courses")
        Log.d("TEST", courseId)
        val courseStudentsCollection = coursesCollection.document(courseId).collection("students")

        courseStudentsCollection
            .get()
            .addOnSuccessListener { result ->
                studentItems.clear()
                Log.d(FIREBASE_TAG, " ---- all students ----")
                for (document in result){
                    val student = document.toObject<Student>()
                    Log.d(FIREBASE_TAG, student.id)
                    studentItems.add(student)
                }
                (ui.StudentList.adapter as StudentAdapter).notifyDataSetChanged()
            }

        ui.StudentList.adapter = StudentAdapter(students = studentItems)
        ui.StudentList.layoutManager = LinearLayoutManager(this)
    }
    inner class StudentHolder(var ui: StudentListItemBinding) : RecyclerView.ViewHolder(ui.root) {

    }
    inner class StudentAdapter(private val students : MutableList<Student>) : RecyclerView.Adapter<StudentHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentHolder {
            val ui = StudentListItemBinding.inflate(layoutInflater, parent, false)
            return StudentHolder(ui)
        }

        override fun getItemCount(): Int {
            return students.size
        }

        override fun onBindViewHolder(holder: StudentHolder, position: Int) {
            val student = students[position]
            holder.ui.studentName.text = student.name
            holder.ui.score.text = "Total Score" + student.totalScore.toString()

            holder.itemView.setOnClickListener{
                val selectedStudentId = student.id.toString()
                val selectedCourseId = ui.toolbar.title.toString()
                val intentToWeekDashboard = Intent(applicationContext, WeeksDashboard::class.java)
                intentToWeekDashboard.putExtra(COURSEID, selectedCourseId)
                intentToWeekDashboard.putExtra(STUDENTID_KEY, selectedStudentId)
                intentToWeekDashboard.putExtra(STUDENTNAME_KEY, student.name)
                startActivity(intentToWeekDashboard)
            }

            holder.ui.floatingActionButton.setOnClickListener {
                val db = Firebase.firestore
                val courseId = ui.toolbar.title.toString()
                studentItems.remove(student)
                val coursesCollection = db.collection("courses").document(courseId).collection("students").document(student.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Student deleted successfully", Toast.LENGTH_SHORT).show()
                            (ui.StudentList.adapter as StudentAdapter).notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Log.e(FIREBASE_TAG, "Error deleting document", it)
                        }
            }
        }

    }
}