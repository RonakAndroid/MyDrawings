package com.divyanshu.androiddraw

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_view.view.*
import java.io.File

const val IMAGE_PATH = "image_path"

class DrawAdapter(private val context: Context) : RecyclerView.Adapter<DrawAdapter.ViewHolder>() {
    var imageList = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_view, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return imageList.size
    }

    fun setList(mArrayList: ArrayList<String>, clear: Boolean = false) {
        if (clear) {
            imageList.clear()
            imageList.addAll(mArrayList)
        }
        this.imageList = mArrayList
        notifyDataSetChanged()
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val path = imageList[holder.adapterPosition]
        Glide.with(context).load(path).into(holder.drawImage)
//        holder.drawImage.setOnClickListener {
//            val intent = Intent(context, ImageActivity::class.java)
//            intent.putExtra(IMAGE_PATH, path)
//            context.startActivity(intent)
//        }
        holder.imgDelete.setOnClickListener {
            removeItem(path, holder.adapterPosition)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val drawImage: ImageView = itemView.image_draw
        val imgDelete: ImageView = itemView.imgDelete
    }

    fun addItem(uri: Uri) {
        imageList.add(uri.toString())
        notifyItemInserted(imageList.size - 1)
    }

    fun removeItem(path: String, position: Int) {
        val fdelete = File(path)
        fdelete.delete()
        if (fdelete.exists()) {
            if (fdelete.delete()) {
                System.out.println("file Deleted :" + path)
            } else {
                System.out.println("file not Deleted :" + path)
            }
        }
        imageList.remove(path)
        notifyItemRemoved(position)
    }
}