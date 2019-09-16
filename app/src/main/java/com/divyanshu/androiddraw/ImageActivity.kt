package com.divyanshu.androiddraw

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.api.load
import com.divyanshu.androiddraw.model.Upload
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_image.*

class ImageActivity : AppCompatActivity(), View.OnClickListener {
    lateinit var path: Uri
    val TAG = this::class.java.simpleName
    val database = FirebaseDatabase.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_image)

        imgUploadFile.setOnClickListener(this)
        path = intent.getParcelableExtra(IMAGE_PATH)
        Log.i(TAG, "path>>" + path)
        image_view.load(path)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.imgUploadFile -> {
                if (imgUploadFile.isClickable) {
                    Toast.makeText(this, "imgUploadFile is going to upload", Toast.LENGTH_LONG).show()
                    uploadFileToStorage(path)
                } else {
                    Toast.makeText(this, getString(R.string.alert_twise), Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    private fun uploadFileToStorage(filePath: Uri) {
        //if there is a file to upload
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Uploading")
        progressDialog.show()

        val imagesRef = FirebaseStorage.getInstance().reference
                .child("images/" + System.currentTimeMillis().toString() + ".png")

        imagesRef.putFile(filePath)
                .addOnSuccessListener({ taskSnapshot ->
                    //if the upload is successfull
                    //hiding the progress dialog
                    progressDialog.dismiss()
                    val mDownloadUrl = taskSnapshot.storage.downloadUrl
                    //and displaying a success toast
                    Log.i(TAG, "mDownloadUrl>>" + mDownloadUrl)
                    Toast.makeText(applicationContext, "File Uploaded ", Toast.LENGTH_LONG).show()
                    //onDownload

                    val ref = imagesRef.child("images/" + System.currentTimeMillis().toString())
                    ref.putFile(filePath)
                            .addOnSuccessListener {
                                progressBar.visibility = View.INVISIBLE

                                ref.downloadUrl.addOnSuccessListener {
                                    val imageModel = Upload()
                                    val uploadId: String? = database.push().key
                                    imageModel.id = uploadId.toString()
                                    imageModel.url = it.toString()
                                    database.child("images").child(uploadId!!).setValue(imageModel)
                                }
                            }
                })
                .addOnFailureListener({ exception ->
                    //if the upload is not successfull
                    //hiding the progress dialog
                    progressDialog.dismiss()

                    //and displaying error message
                    Toast.makeText(applicationContext, exception.message, Toast.LENGTH_LONG).show()
                })
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i(TAG, "isSuccessful" + task.result)


                        val ref = imagesRef
                        val uploadTask = ref.putFile(filePath)
                        val urlTask = uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                    Log.i(TAG, "isSuccessful>>" + it.message)
                                }
                            }
                            return@Continuation ref.downloadUrl
                        })

                        startActivity(Intent(this, ShowAllActivity::class.java))
                        imgUploadFile.isClickable = false
                    } else {
                        Log.i(TAG, "isNOTSuccessful" + task.exception)
                    }
                }
    }
}
