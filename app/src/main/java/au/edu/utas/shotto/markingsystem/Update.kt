package au.edu.utas.shotto.markingsystem

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.view.size
import au.edu.utas.shotto.markingsystem.databinding.ActivityUpdateBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase


class Update : AppCompatActivity() {
    private lateinit var ui : ActivityUpdateBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityUpdateBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val week = intent.getStringExtra(SELECTEDWEEK).toString()
        ui.txtWeek.text = week

        val studentid = intent.getStringExtra(SELECTEDSTUDENT_ID).toString()
        ui.txtSid.text = studentid

        val studentname = intent.getStringExtra(STUDENTNAME_KEY).toString()
        ui.stdNameTextField.setText(studentname)

        val courseId = intent.getStringExtra(SELECTEDCOURSE_ID).toString()

        //if record exists, display the information in the collection
        val db = Firebase.firestore
        val studentRecordCollection = db.collection("courses").document(courseId)
            .collection("students").document(studentid)
            .collection("weeks").document(week)

        studentRecordCollection
            .get()
            .addOnSuccessListener { result ->
                val record = result.toObject<Week>()
                if (record != null){
                    if (record.MarkScheme != ""){
                        val spinnerSize = ui.spinnerMarkscheme.count
                        Log.d("TEST", "${spinnerSize}")
                        Log.d("TEST", record.MarkScheme)
                        for ( i in 0..(spinnerSize-1)){
                            Log.d("TEST", ui.spinnerMarkscheme.getItemAtPosition(i).toString())
                            if (ui.spinnerMarkscheme.getItemAtPosition(i).toString() == record.MarkScheme){
                                ui.spinnerMarkscheme.setSelection(i)
                            }
                        }
                    }
                    if (record.Grade != ""){
                        val spinnerSize = ui.spinnerGrade.count
                        Log.d("TEST", "${spinnerSize}")
                        Log.d("TEST", record.Grade)
                        for ( i in 0..(spinnerSize-1)){
                            Log.d("TEST", ui.spinnerGrade.getItemAtPosition(i).toString())
                            if (ui.spinnerGrade.getItemAtPosition(i).toString() == record.Grade){
                                ui.spinnerGrade.setSelection(i)
                            }
                        }

                    }
                    if (record.Attendance != ""){
                        if (record.Attendance == "Attended"){
                            ui.checkBox.isChecked = true
                        }
                    }
                }
            }

        ui.spinnerMarkscheme.adapter = ArrayAdapter<String>(
             this,
             R.layout.spinner_grade_item,
             resources.getStringArray(R.array.MarkingScheme)
        )

        val adapter1 = ArrayAdapter<String>(
            this,
            R.layout.spinner_grade_item,
            resources.getStringArray(R.array.ABCgradesScheme)
        )
        val adapter2 = ArrayAdapter<String>(
            this,
            R.layout.spinner_grade_item,
            resources.getStringArray(R.array.HDgradesScheme)
        )

        ui.spinnerMarkscheme.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("TAG", selectedItem)
                if (selectedItem == "HD+/HD/DN/CR/PP/NN"){
                    ui.spinnerGrade.adapter = adapter2
                } else {
                    ui.spinnerGrade.adapter = adapter1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        ui.spinnerGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("TAG", selectedItem)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        ui.btnUpdateConfirm.setOnClickListener {
            var selectedMarkScheme = ui.spinnerMarkscheme.selectedItem.toString()
            var selectedGrade = ui.spinnerGrade.selectedItem.toString()
            var attendanceStatus : String
            if (ui.checkBox.isChecked){
                attendanceStatus = "Attended"
            }else{
                attendanceStatus = "Not Attended"
            }

            var newStudentRecord = Week(
                WeekNo = week,
                StudentId = studentid,
                    StudentName = ui.stdNameTextField.text.toString(),
                MarkScheme = selectedMarkScheme,
                Grade = selectedGrade,
                Attendance = attendanceStatus
            )

            //updateTotalScore(attendanceStatus, )

            studentRecordCollection
                .set(newStudentRecord)
                .addOnSuccessListener {
                    Log.d(FIREBASE_TAG, "student record created.")
                    finish()
                }
                .addOnFailureListener {
                    Log.d(FIREBASE_TAG, "Error writing document(student record)", it)
                }

        }

    }

    fun updateTotalScore(){

    }
}