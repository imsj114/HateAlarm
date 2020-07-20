package com.example.madcampweek2.ui.maps.list

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.madcampweek2.R
import com.example.madcampweek2.model.MapUser

class UserListViewAdapter(val context: Context, val userList: List<MapUser>) : RecyclerView.Adapter<UserListViewAdapter.Holder>() {

    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView : ImageView = itemView.findViewById<ImageView>(R.id.profile_map_user)
        private val nameTextView: TextView = itemView.findViewById(R.id.name_map_user)
        private val blockedTextView: TextView = itemView.findViewById(R.id.blocked_map_user)
        private val deleteImageView: ImageView = itemView.findViewById(R.id.img_delete)

        fun bind(user : MapUser){
            Glide.with(context)
                .load(if(user.imageURI != "") user.imageURI else R.drawable.apeach)
                .fitCenter()
                .override(300, 300)
                .into(imageView)
            nameTextView.text = user.name
            blockedTextView.text = if(user.blocked) "손절" else "아직 친구"
            deleteImageView.setImageResource(if(user.blocked) R.drawable.ic_baseline_add_circle_24 else R.drawable.ic_baseline_remove_circle_24)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_map_user, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int  = userList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(userList[position])
    }
}