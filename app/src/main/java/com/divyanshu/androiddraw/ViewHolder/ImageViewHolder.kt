package com.divyanshu.androiddraw.ViewHolder

import android.view.View
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.divyanshu.androiddraw.R

class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var ivImageView: ImageView = itemView.findViewById(R.id.image_draw) as ImageView
    var ivRemove: ImageView = itemView.findViewById(R.id.imgDelete) as ImageView
}