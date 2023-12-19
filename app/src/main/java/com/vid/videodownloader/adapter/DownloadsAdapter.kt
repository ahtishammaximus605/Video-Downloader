package com.vid.videodownloader.adapter

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.skydoves.powermenu.MenuAnimation
import com.skydoves.powermenu.PowerMenu
import com.skydoves.powermenu.PowerMenuItem
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.ItemDownloadBinding
import com.vid.videodownloader.databinding.ItemDownloadedBinding
import com.vid.videodownloader.interfaces.OnItemClick
import com.vid.videodownloader.model.Download
import com.vid.videodownloader.utils.FileUtility
import com.vid.videodownloader.utils.FileUtility.Companion.getDurationString
import com.vid.videodownloader.utils.StorageSharedPref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

import java.io.File


class DownloadsAdapter(val tap: OnItemClick<Download>) :
    ListAdapter<Download, DownloadsAdapter.BaseViewHolder>(ADAPTER_COMPARATOR) {
    var context: Context? = null

    abstract class BaseViewHolder(itemBinding: ViewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {
        abstract fun bind(model: Download)
    }

    inner class DownloadHolder(private val itemBinding: ItemDownloadBinding) :
        BaseViewHolder(itemBinding) {

        override fun bind(model: Download) {
            val coroutineScope = CoroutineScope(Dispatchers.IO)
            itemBinding.txtName.text = model.name
            itemBinding.progressBar.progress = model.progress
            if (model.progress == null) {

                itemBinding.txtSize.text = "" + 0 + "%"
            } else {
                itemBinding.txtSize.text = "" + model.progress + "%"

            }
            itemBinding.imageView2.setOnClickListener {
                tap.itemClickResult(model, "onCancel")
            }


        }
    }

    inner class DownloadedHolder(private val itemBinding: ItemDownloadedBinding) :
        BaseViewHolder(itemBinding) {

        override fun bind(model: Download) {


            val dString = model.duration?.getDurationString()
            itemBinding.txtName.text = model.name
            itemBinding.txtSize.text = model.size?.let { FileUtility().getSIzeMB(it) }
            itemBinding.txtDuration.text = dString


            if (model.path != null) {
                var uri = Uri.parse(model.path)
                val file = File(model.path.toString())
                if (file.exists())
                    uri = Uri.fromFile(file)
                if (uri != null) {
                    context?.let {
                        Glide.with(it).asBitmap()
                            .load(uri)
                            .placeholder(R.drawable.down_arrow)
                            .disallowHardwareConfig()
                            .error(R.drawable.down_arrow)
                            .transform(CenterCrop(), RoundedCorners(20))
                            .into(itemBinding.imgIcon)
                    }
                }
                itemBinding.constraintLayout7.setOnClickListener {
                    tap.itemClickResult(model, "onPlay")

                }
                val powerMenu = PowerMenu.Builder(context!!)
                val pb = powerMenu.addItem(PowerMenuItem("Copy Link", R.drawable.copy_link, false))
                    .addItem(PowerMenuItem("Rename", R.drawable.rename_ic, false))
                    .addItem(PowerMenuItem("Play Video", R.drawable.play_ic, false))
                    .addItem(PowerMenuItem("Share", R.drawable.share_ic, false))
                    .addItem(PowerMenuItem("Delete", R.drawable.delete_ic, false))
                    .addItem(PowerMenuItem("Mirroring", R.drawable.cast_ic, false))
                    .setAnimation(MenuAnimation.SHOWUP_TOP_RIGHT) // Animation start point (TOP | LEFT)
                    .setMenuRadius(10f)
                    .setMenuShadow(10f)
                    .setIconPadding(15)
                    .setPadding(10)
                    .setIconSize(16)
                    .setTextColor(context!!.resources.getColor(R.color.gray))
                    .setSelectedTextColor(Color.GRAY)
                    .setMenuColor(Color.WHITE)
                    .setSelectedMenuColor(context!!.resources.getColor(R.color.white))
                    .build()
                pb?.setOnMenuItemClickListener { _, item ->
                    when (item?.title) {
                        "Copy Link" -> {
//                            var u = ""
//                            val vids = StorageVideos.get()
//                            vids?.forEach {
//                                if(it?.path?.replace("file://","") == model.path)
//                                   u = it?.fbUrl ?: ""
//                            }
                            val text = model.fbUrl
                            val clipboard =
                                context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip: ClipData = ClipData.newPlainText("copied text", text)
                            clipboard.setPrimaryClip(clip)
                            StorageSharedPref.save("copied", text)
                            Toast.makeText(context, "Link copied.", Toast.LENGTH_SHORT)
                                .show()

                        }

                        "Rename" -> {
                            model.let { tap.itemClickResult(it, "onRename") }
                        }

                        "Play Video" -> {
                            model.let { tap.itemClickResult(it, "onPlay") }
                        }

                        "Share" -> {
                            model.let { tap.itemClickResult(it, "onShare") }
                        }

                        "Repost" -> {
                            model.let { tap.itemClickResult(it, "onRepost") }
                        }

                        "Delete" -> {
                            model.let { tap.itemClickResult(it, "onDelete") }
                        }

                        "Mirroring" -> {
                            model.let { tap.itemClickResult(it, "onMirror") }
                        }
                    }
                    pb.dismiss()
                }

                itemBinding.imageView2.setOnClickListener {
                    pb?.showAsAnchorLeftTop(it, -260, 0)
                }
            }
        }


    }

    override fun getItemViewType(position: Int): Int {
        return if (getItem(position).isDownloading) 0 else 1

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        context = parent.context

        return when (viewType) {
            1 -> {
                val itemBinding = ItemDownloadedBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                DownloadedHolder(itemBinding)
            }

            else -> {
                val itemBinding =
                    ItemDownloadBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                DownloadHolder(itemBinding)
            }
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object {
        private val ADAPTER_COMPARATOR = object : DiffUtil.ItemCallback<Download>() {
            override fun areItemsTheSame(oldItem: Download, newItem: Download): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldItem: Download, newItem: Download): Boolean {
                return oldItem.path == newItem.path
            }
        }
    }
}