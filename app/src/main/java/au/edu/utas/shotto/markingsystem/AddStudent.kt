package au.edu.utas.shotto.markingsystem

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import au.edu.utas.shotto.markingsystem.databinding.ActivityAddCourseBinding
import au.edu.utas.shotto.markingsystem.databinding.ActivityAddStudentBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

val students = mutableListOf<Student>()
const val REQUEST_IMAGE_CAPTURE = 1

class AddStudent : AppCompatActivity() {
    private lateinit var ui : ActivityAddStudentBinding
    private var photoURI: Uri? = null
    private val PICK_IMAGE_REQUEST = 71

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ui = ActivityAddStudentBinding.inflate(layoutInflater)
        setContentView(ui.root)

        val courseId = intent.getStringExtra(COURSEID).toString()

        ui.cameraBtn.setOnClickListener {
            requestToTakeAPicture()
        }

        val db = Firebase.firestore
        val courseStudentCollection = db.collection("courses").document(courseId).collection("students")


        ui.btnAddStudent.setOnClickListener {
            val studentId = ui.txtStudentId.text.toString()
            val studentName = ui.studentNameTextField.text.toString()
            val newStudent = Student(
                id = studentId,
                name = studentName,
                    totalScore = 0.0
            )
            courseStudentCollection
                .document(studentId).set(newStudent)
                .addOnSuccessListener {
                    Log.d(FIREBASE_TAG, "student created.")
                    finish()
                }
                .addOnFailureListener{
                    Log.d(FIREBASE_TAG, "Error writing document(student)", it)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestToTakeAPicture(){
        requestPermissions(
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_IMAGE_CAPTURE
        )

    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when(requestCode){
            REQUEST_IMAGE_CAPTURE -> {
                if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                    //Permission is granted.
                    takeAPicture()
                } else {
                    Toast.makeText(this, "Cannot access camera, permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }

    }

    private fun takeAPicture(){
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File = createImageFile()!!
        photoURI= FileProvider.getUriForFile(this, "au.edu.utas.shotto.markingsystem", photoFile)
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
        try{
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
        } catch (e: ActivityNotFoundException){

        }

    }

    lateinit var currentPhotoPath: String

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
                "JPEG_${timeStamp}_",
                ".jpg",
                storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(((requestCode == REQUEST_IMAGE_CAPTURE)||(requestCode==PICK_IMAGE_REQUEST)) && resultCode == RESULT_OK){
            setPic(ui.imageView)
        }
    }

    private fun setPic(imageView: ImageView){
        val targetW: Int = imageView.measuredWidth
        val targetH: Int = imageView.measuredHeight

        val bmOptions = BitmapFactory.Options().apply {
            inJustDecodeBounds = true

            BitmapFactory.decodeFile(currentPhotoPath, this)

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            val scaleFactor: Int = Math.max(1, Math.min(photoW/targetW, photoH/targetH))

            inJustDecodeBounds = false
            inSampleSize = scaleFactor
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also{ bitmap ->
            imageView.setImageBitmap(bitmap)
        }
    }
}