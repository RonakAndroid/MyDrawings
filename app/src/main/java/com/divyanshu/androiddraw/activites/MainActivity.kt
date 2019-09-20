package com.divyanshu.androiddraw.activites

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.divyanshu.androiddraw.R
import com.divyanshu.androiddraw.Utils.Constants.TOUCH_INPUT_DIR
import com.divyanshu.androiddraw.adapters.DrawAdapter
import com.divyanshu.androiddraw.adapters.IMAGE_PATH
import com.divyanshu.draw.activity.DrawingActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.collections.ArrayList

private const val REQUEST_CODE_DRAW = 101

private const val PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 102

class MainActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName
    val resultList = ArrayList<String>()
    private var uri: Uri? = null
    private var filePath: Uri? = null
    lateinit var adapter: DrawAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        } else {
            adapter = DrawAdapter(this)
            adapter.setList(getFilesPath())
            recycler_view.adapter = adapter
        }
        fab_add_draw.setOnClickListener {
            val intent = Intent(this, DrawingActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_DRAW)
        }

        // Write a message to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference(getString(R.string.message))

        //test code removeable
        myRef.setValue(getString(R.string.str_hello_msg))
    }

    fun getFilesPath(): ArrayList<String> {
        val imageDir = Environment.DIRECTORY_PICTURES + TOUCH_INPUT_DIR
        val path = Environment.getExternalStoragePublicDirectory(imageDir)
        path.mkdirs()
        val imageList = path.listFiles()
        imageList.forEach { imagePath ->
            resultList.add(imagePath.absolutePath)
            Log.i(TAG, "imagePath.absolutePath" + imagePath.absolutePath)
        }
        return resultList
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (data != null && resultCode == Activity.RESULT_OK) {
            filePath = data.data
            when (requestCode) {
                REQUEST_CODE_DRAW -> {
                    val result = data.getByteArrayExtra(getString(R.string.bitmap))
                    val bitmap = BitmapFactory.decodeByteArray(result, 0, result.size)
                    showSaveDialog(bitmap)
                }
            }
        }
    }

    private fun showSaveDialog(bitmap: Bitmap) {
        val alertDialog = AlertDialog.Builder(this)
        val dialogView = layoutInflater.inflate(R.layout.dialog_save, null)
        alertDialog.setView(dialogView)
        val fileNameEditText: EditText = dialogView.findViewById(R.id.editText_file_name)
        val filename = UUID.randomUUID().toString()
//        val filename = System.currentTimeMillis()
        fileNameEditText.setSelectAllOnFocus(true)
        fileNameEditText.setText(filename)
        alertDialog.setTitle(getString(R.string.save_drawing))
                .setPositiveButton(R.string.ok) { _, _ -> saveImage(bitmap, fileNameEditText.text.toString()) }
                .setNegativeButton(R.string.cancel) { _, _ -> }

        val dialog = alertDialog.create()
        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        dialog.show()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    adapter = DrawAdapter(this)
                    adapter.setList(getFilesPath())
                    recycler_view.adapter = adapter
                } else {
                    finish()
                }
                return
            }
            else -> {
            }
        }
    }

    private fun saveImage(bitmap: Bitmap, fileName: String) {
        val imageDir = Environment.DIRECTORY_PICTURES + TOUCH_INPUT_DIR
        val path = Environment.getExternalStoragePublicDirectory(imageDir)

        Log.e("path", path.toString())
        val file = File(path, "$fileName.png")
        path.mkdirs()
        file.createNewFile()
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()
        updateRecyclerView(Uri.fromFile(file))
        //URI to be stored in firebaseFirestore
        uri = Uri.fromFile(file)
        notifyuser()
    }

    private fun notifyuser() {
        val intent = Intent(this, ImageActivity::class.java)
        intent.putExtra(IMAGE_PATH, uri)
        //save to File Store android
        Log.i(TAG, "uri>>" + uri.toString())
        showNotification(this, "Notification", "New painting is saved.", intent)
    }

    fun showNotification(context: Context, title: String, body: String, intent: Intent) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notificationId = System.currentTimeMillis().toInt()

        val channelId = getString(R.string.cannnelid)
        val channelName = getString(R.string.channel_name)
        val importance = NotificationManager.IMPORTANCE_HIGH

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    channelId, channelName, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        val mBuilder = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)

        val stackBuilder = TaskStackBuilder.create(context)
        stackBuilder.addNextIntent(intent)
        val resultPendingIntent = stackBuilder.getPendingIntent(
                System.currentTimeMillis().toInt(),
                PendingIntent.FLAG_UPDATE_CURRENT
        )
        mBuilder.setContentIntent(resultPendingIntent)
        mBuilder.setAutoCancel(true)
        notificationManager.notify(notificationId, mBuilder.build())
    }

    private fun updateRecyclerView(uri: Uri) {
        adapter.addItem(uri)
        adapter.setList(resultList, true)
    }
}