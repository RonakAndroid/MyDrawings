package com.divyanshu.androiddraw.activites

import android.app.Dialog
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import coil.api.load
import com.divyanshu.androiddraw.R
import com.divyanshu.androiddraw.ViewHolder.ImageViewHolder
import com.divyanshu.androiddraw.model.Upload
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.activity_show_all.*
import kotlinx.android.synthetic.main.custom_dialog.*

class ShowAllActivity : AppCompatActivity() {
    private val LOG_TAG = this::class.java.simpleName
    val firebaseRef = FirebaseDatabase.getInstance().getReference(getString(R.string.images))
    lateinit var fireBaseAdapter: FirebaseRecyclerAdapter<Upload, ImageViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all)

        setUpFirebaseAdapter()
        recyclerview.setHasFixedSize(true)
    }

    private fun setUpFirebaseAdapter() {
        fireBaseAdapter = object : FirebaseRecyclerAdapter<Upload, ImageViewHolder>(

                Upload::class.java,
                R.layout.item_view, ImageViewHolder::class.java, firebaseRef) {

            override fun populateViewHolder(imageViewHolder: ImageViewHolder, upload: Upload, position: Int) {
                progressBar?.visibility = View.GONE
                imageViewHolder.ivImageView.load(upload.url)
                Log.i(LOG_TAG, "imageModel.url>>>" + upload.url)
                imageViewHolder.ivImageView.setOnClickListener {
                    showDialog(upload)
                }
                imageViewHolder.ivRemove.setOnClickListener {
                    val builder = AlertDialog.Builder(this@ShowAllActivity)
                    builder.setMessage(getString(R.string.confirm_before_delete))
                            .setTitle((R.string.alert))
                            .setPositiveButton((android.R.string.ok)) { dialog, _ ->
                                upload.id.let { firebaseRef.child(it).removeValue() }
                                dialog.dismiss()
                            }
                            .setNegativeButton(android.R.string.no) { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                }
            }

            private fun showDialog(imageModel: Upload?) {
                val imageDialog = Dialog(this@ShowAllActivity).apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setContentView(R.layout.custom_dialog)
                }
                imageDialog.goProDialogImage.load(imageModel?.url)
                imageDialog.window?.setBackgroundDrawable(
                        ColorDrawable(ContextCompat.getColor(this@ShowAllActivity, android.R.color.transparent))
                )
                imageDialog.show()
            }

            override fun onDataChanged() {
                super.onDataChanged()
                progressBar?.visibility = View.GONE
                fireBaseAdapter.notifyDataSetChanged()
            }
        }
        recyclerview.adapter = fireBaseAdapter
    }
}