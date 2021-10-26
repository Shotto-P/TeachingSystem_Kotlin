package au.edu.utas.shotto.markingsystem

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import au.edu.utas.shotto.markingsystem.databinding.ActivityWeeksDashboardBinding
import au.edu.utas.shotto.markingsystem.databinding.WeekListItemBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

val weeksItems = arrayOf<String>(
    "Week 01",
    "Week 02",
    "Week 03",
    "Week 04",
    "Week 05",
    "Week 06",
    "Week 07",
    "Week 08",
    "Week 09",
    "Week 10",
    "Week 11",
    "Week 12"
)
const val SELECTEDCOURSE_ID : String = "SELECTCOURSE"
const val SELECTEDSTUDENT_ID : String = "SELECTSTUDENT"
const val SELECTEDWEEK : String = "SELECTWEEK"

class WeeksDashboard : AppCompatActivity() {
    private lateinit var ui : ActivityWeeksDashboardBinding
    var studentName : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityWeeksDashboardBinding.inflate(layoutInflater)
        setContentView(ui.root)

        //get the courseid and studentid from previous activity
        val courseId  = intent.getStringExtra(COURSEID).toString()
        val studentId = intent.getStringExtra(STUDENTID_KEY).toString()
        studentName = intent.getStringExtra(STUDENTNAME_KEY).toString()

        //set the toolbar title with courseid
        ui.toolbarWeek.title = courseId
        ui.toolbarSid.text = studentId


        val db = Firebase.firestore
        val studentCollection = db.collection("courses").document(courseId)
            .collection("students").document(studentId).collection("weeks")

        //add weeks into the student collection
        for(week in weeksItems){
            /*var newWeek = Week(
                WeekNo = week
            )*/
            studentCollection.document(week)
        }
        ui.weekList.adapter = WeekAdapter(weeks = weeksItems)
        ui.weekList.layoutManager = LinearLayoutManager(this)

    }
    inner class WeekHolder(var ui : WeekListItemBinding) : RecyclerView.ViewHolder(ui.root){

    }
    inner class WeekAdapter(private val weeks : Array<String>) : RecyclerView.Adapter<WeekHolder>(){
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeekHolder {
            val ui = WeekListItemBinding.inflate(layoutInflater, parent, false)
            return WeekHolder(ui)
        }

        override fun onBindViewHolder(holder: WeekHolder, position: Int) {
            val week = weeks[position]
            holder.ui.weekItem.text = week

            holder.itemView.setOnClickListener{
                val selectedCourseId = ui.toolbarWeek.title
                val selectedStudentId = ui.toolbarSid.text
                val selectedWeek = week
                val intentToUpdate = Intent(applicationContext, Update::class.java)
                intentToUpdate.putExtra(SELECTEDCOURSE_ID, selectedCourseId)
                intentToUpdate.putExtra(SELECTEDSTUDENT_ID, selectedStudentId)
                intentToUpdate.putExtra(SELECTEDWEEK, selectedWeek)
                intentToUpdate.putExtra(STUDENTNAME_KEY, studentName)
                startActivity(intentToUpdate)
            }
        }

        override fun getItemCount(): Int {
            return weeks.size
        }

    }
}