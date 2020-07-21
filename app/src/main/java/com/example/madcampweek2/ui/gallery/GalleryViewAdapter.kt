package com.example.madcampweek2.ui.gallery

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.madcampweek2.R
import com.example.madcampweek2.model.Image

class GalleryViewAdapter(private val context : Context, private var imageList: MutableList<Image>, val itemLongClick: (Int) -> Boolean)
    : RecyclerView.Adapter<GalleryViewAdapter.Holder>() {

    inner class Holder(itemView: View, itemLongClick: (Int) -> Boolean) : RecyclerView.ViewHolder(itemView) {
        private val imageView : ImageView = itemView.findViewById<ImageView>(R.id.id_image)

        fun bind(img : Image, position : Int){
            Glide.with(context)
                .load(img.getUrl())
                .fitCenter()
                .override(300, 300)
                .placeholder(R.drawable.image_load)
                .into(imageView)
            imageView.setOnLongClickListener{ itemLongClick(position) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        return Holder(view, itemLongClick)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(imageList[position], position)
    }

    override fun getItemCount(): Int = imageList.size

    fun setData(data: List<Image>) {
        imageList = data.toMutableList()
        notifyDataSetChanged()
    }
}

