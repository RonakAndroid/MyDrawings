package com.divyanshu.androiddraw.activites

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import coil.api.load
import com.divyanshu.androiddraw.Extenstions.showAlert
import com.divyanshu.androiddraw.R
import com.divyanshu.androiddraw.Utils.Logger
import com.divyanshu.androiddraw.adapters.IMAGE_PATH
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
        btnShowAll.setOnClickListener(this)

        path = intent.getParcelableExtra(IMAGE_PATH)
        Logger.i(TAG, "path>>" + path)
        image_view.load(path)
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.btnShowAll -> {
                if (!isInternetOn()) {
                    showAlert(this, getString(R.string.check_Internet_hint))
                } else {
                    startActivity(Intent(this, ShowAllActivity::class.java))
                }
            }
            R.id.imgUploadFile -> {
                if (!isInternetOn()) {
                    showAlert(this, getString(R.string.check_Internet_hint))
                } else {
                    startActivity(Intent(this, ShowAllActivity::class.java))
                }
                if (imgUploadFile.isClickable) {
                    uploadFileToStorage(path)
                } else {
                    showAlert(this, getString(R.string.alert_twise))
                }
            }
        }
    }

    private fun uploadFileToStorage(filePath: Uri) {
        //check permission for internet
        progressBar.visibility = View.VISIBLE
        val imagesRef = FirebaseStorage.getInstance().reference
                .child(getString(R.string.path_string) + System.currentTimeMillis().toString() + ".png")
        imagesRef.putFile(filePath)
                .addOnSuccessListener { taskSnapshot ->
                    //if the upload is successfull
                    //hiding the progress dialog
                    progressBar.visibility = View.INVISIBLE
                    val mDownloadUrl = taskSnapshot.storage.downloadUrl
                    //and displaying a success toast
                    Logger.i(TAG, "mDownloadUrl>>" + mDownloadUrl)
                    //onDownload
                    imagesRef.putFile(filePath)
                            .addOnSuccessListener {
                                progressBar.visibility = View.INVISIBLE

                                imagesRef.downloadUrl.addOnSuccessListener {
                                    val imageModel = Upload()
                                    val uploadId: String? = database.push().key
                                    imageModel.id = uploadId.toString()
                                    imageModel.url = it.toString()
                                    database.child(getString(R.string.images)).child(uploadId!!).setValue(imageModel)
                                }
                            }
                }
                .addOnFailureListener({ exception ->
                    //if the upload is not successfull
                    //hiding the progress dialog
                    progressBar.visibility = View.INVISIBLE
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
                                    Logger.i(TAG, "isSuccessful>>" + it.message)
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

    fun isInternetOn(): Boolean {
        val connec = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // ARE WE CONNECTED TO THE NET
        if (connec.getNetworkInfo(0).state == NetworkInfo.State.CONNECTED || connec.getNetworkInfo(1).state == NetworkInfo.State.CONNECTED) {
            // MESSAGE TO SCREEN FOR TESTING (IF REQ)
            //Toast.makeText(this, connectionType + ” connected”, Toast.LENGTH_SHORT).show();
            return true
        } else if (connec.getNetworkInfo(0).state == NetworkInfo.State.DISCONNECTED || connec.getNetworkInfo(1).state == NetworkInfo.State.DISCONNECTED) {
            return false
        }
        return false
    }
}
