package com.divyanshu.androiddraw

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var ivImageView: ImageView = itemView.findViewById(R.id.image_draw) as ImageView
    var ivRemove: ImageView = itemView.findViewById(R.id.imgDelete) as ImageView
}