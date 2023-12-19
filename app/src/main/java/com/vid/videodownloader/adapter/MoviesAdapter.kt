package com.vid.videodownloader.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.ItemMovieBinding
import com.vid.videodownloader.interfaces.OnItemClick
import com.vid.videodownloader.model.VideoModel

class MoviesAdapter (val tap: OnItemClick<VideoModel>)
    : ListAdapter<VideoModel, MoviesAdapter.MovieHolder>(ADAPTER_COMPARATOR)
{
    var context : Context? = null

    inner class MovieHolder(private val itemBinding: ItemMovieBinding) :  RecyclerView.ViewHolder(itemBinding.root){
        fun bind(model: VideoModel) {
            itemBinding.txtName.text = model.videoTitle
            context?.let {
                Glide.with(it).asBitmap()
                    .load(model.videoUri)
                    .placeholder(R.drawable.down_arrow)
                    .disallowHardwareConfig()
                    .error(R.drawable.down_arrow)
                    .transform( CenterCrop(), RoundedCorners(20))
                    .into(itemBinding.imgIconTrailer)
            }
            itemBinding.constraintLayout7.setOnClickListener {
              tap.itemClickResult(model,"")
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieHolder {
        context = parent.context
        val itemBinding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MovieHolder(itemBinding)
    }

    override fun onBindViewHolder(holder: MovieHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val ADAPTER_COMPARATOR = object : DiffUtil.ItemCallback<VideoModel>() {
            override fun areItemsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: VideoModel, newItem: VideoModel): Boolean {
                return oldItem.id == newItem.id
            }
        }
    }
}