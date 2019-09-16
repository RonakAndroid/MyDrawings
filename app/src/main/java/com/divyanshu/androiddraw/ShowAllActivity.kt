package com.divyanshu.androiddraw

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import coil.api.load
import com.divyanshu.androiddraw.model.Upload
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_image.*
import kotlinx.android.synthetic.main.activity_show_all.*


class ShowAllActivity : AppCompatActivity() {
    val TAG = this::class.java.simpleName
    val firebaseRef = FirebaseDatabase.getInstance().getReference("images")
    lateinit var fireBaseAdapter: FirebaseRecyclerAdapter<Upload, ImageViewHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_all)

        setUpFirebaseAdapter()
        recyclerview.setHasFixedSize(true)
        recyclerview.layoutManager = LinearLayoutManager(this)
    }

    private fun setUpFirebaseAdapter() {
        fireBaseAdapter = object : FirebaseRecyclerAdapter<Upload, ImageViewHolder>(Upload::class.java,
                R.layout.item_view, ImageViewHolder::class.java, firebaseRef) {

            override fun populateViewHolder(imageViewHolder: ImageViewHolder?, upload: Upload?, position: Int) {
                progressBar?.visibility = View.GONE
                imageViewHolder?.ivImageView?.load(upload?.url)

                Log.i(TAG, "imageModel.url>>>" + upload?.url)

                imageViewHolder?.ivRemove?.setOnClickListener() {
                    val builder = AlertDialog.Builder(this@ShowAllActivity)

                    builder.setMessage("Are You Sure want to Delete this Image?")
                            .setTitle(("Alert"))
                            .setPositiveButton(("YES")) { dialog, _ ->

                                upload?.id.let { it -> firebaseRef.child(it!!).removeValue() }
                                Toast.makeText(this@ShowAllActivity, "image deleted from FireBaseStorage", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                            .setNegativeButton("NO") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()
                }
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