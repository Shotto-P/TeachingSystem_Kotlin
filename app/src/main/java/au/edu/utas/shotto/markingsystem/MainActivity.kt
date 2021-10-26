package au.edu.utas.shotto.markingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.shotto.markingsystem.databinding.ActivityMainBinding
import au.edu.utas.shotto.markingsystem.databinding.CourseListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase

val items = mutableListOf<Course>()
const val FIREBASE_TAG = "FirebaseLogging"
const val SELECTED_COURSE_KEY : String = "SELECTED_COURSE"

class MainActivity : AppCompatActivity() {
    private lateinit var ui : ActivityMainBinding

    override fun onResume() {
        super.onResume()

        ui.CourseList.adapter?.notifyDataSetChanged()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityMainBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val button = ui.buttonAddCourse
        button.setOnClickListener {
            val intent = Intent(this, AddCourse::class.java)
            startActivity(intent)
        }

        val db = Firebase.firestore
        var coursesCollection = db.collection("courses")

        /*get the all courses data from the database*/
        coursesCollection
            .get()
            .addOnSuccessListener { result ->
                items.clear()
                Log.d(FIREBASE_TAG, "------ all courses ------")
                for(document in result){
                    val course = document.toObject<Course>()
                    Log.d(FIREBASE_TAG, course.name.toString())
                    items.add(course)
                }
                (ui.CourseList.adapter as CourseAdapter).notifyDataSetChanged()
            }

        ui.CourseList.adapter = CourseAdapter(courses = items)
        ui.CourseList.layoutManager = LinearLayoutManager(this)


    }
    inner class CourseHolder(var ui : CourseListItemBinding) : RecyclerView.ViewHolder(ui.root){

    }
    inner class CourseAdapter(private val courses : MutableList<Course>) : RecyclerView.Adapter<CourseHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseHolder {
            val ui = CourseListItemBinding.inflate(layoutInflater, parent, false)
            return CourseHolder(ui)
        }

        override fun getItemCount(): Int {
            return courses.size
        }

        override fun onBindViewHolder(holder: CourseHolder, position: Int) {
            val course = courses[position]
            holder.ui.courseName.text = course.name.toString()
            holder.ui.courseId.text = course.id.toString()

            holder.itemView.setOnClickListener{
                val intentToHome = Intent(applicationContext, CourseHomepage::class.java)
                var selectedCourseId = holder.ui.courseId.text
                intentToHome.putExtra(SELECTED_COURSE_KEY, selectedCourseId)
                startActivity(intentToHome)
            }

            holder.ui.deleteBtn.setOnClickListener {
                val db = Firebase.firestore
                var coursesCollection = db.collection("courses")
                courses.remove(course)
                coursesCollection.document(course.id).delete()
                        .addOnSuccessListener {
                            Toast.makeText(applicationContext, "Course deleted successfully", Toast.LENGTH_SHORT).show()
                            (ui.CourseList.adapter as CourseAdapter).notifyDataSetChanged()
                        }
                        .addOnFailureListener {
                            Log.e(FIREBASE_TAG, "Error deleting document", it)
                        }
            }
        }
    }
}