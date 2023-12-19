package com.vid.videodownloader.background

import android.Manifest
import android.app.DownloadManager
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.database.getLongOrNull
import androidx.core.database.getStringOrNull
import com.permissionx.guolindev.PermissionX
import com.vid.videodownloader.BuildConfig
import com.vid.videodownloader.model.Download
import com.vid.videodownloader.utils.FileUtility.Companion.getDuration
import com.vid.videodownloader.utils.FileUtility.Companion.getDurationString
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.DownloadLinkActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import kotlin.collections.ArrayList

private val PROJECTION = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.SIZE,MediaStore.Video.Media.DATA)
private val PROJECTION2 = arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME,
    MediaStore.Video.Media.SIZE,MediaStore.Video.Media.DURATION,MediaStore.Video.Media.RELATIVE_PATH)
private const val QUERY = MediaStore.Video.Media.DATA + " = ?"

class VideoRep(var context : Context?) {

    private val collection =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Video.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        ) else MediaStore.Video.Media.EXTERNAL_CONTENT_URI

    suspend fun getLocalUri(filename: String): Uri? =
        withContext(Dispatchers.IO) {
            val resolver = context?.contentResolver

            resolver?.query(collection, PROJECTION, QUERY, arrayOf(filename), null)
                ?.use { cursor ->
                    if (cursor.count > 0) {
                        cursor.moveToFirst()
                        return@withContext ContentUris.withAppendedId(
                            collection,
                            cursor.getLong(0)
                        )
                    }
                }

            null
        }
    suspend fun getVideos(folder: File?): ArrayList<Download?>? =
        withContext(Dispatchers.IO) {
                val uris : ArrayList<Download?> = ArrayList()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val files = folder?.listFiles()
                files?.sortBy { it.lastModified() }
                //to make function look bigger :). We will try to load only the images from internal storage that we have saved in save example.
                files?.filter { file -> file.canRead()}?.map {file->

                    val duration = try {
                        context?.let { it1 -> file.getDuration(it1) }
                                } catch (ex: Exception) {
                                    null
                                }
                    uris.add(
                        Download(
                            file.name,
                            file.absolutePath,
                            file.length(),
                            duration,
                            null
                        )
                    )
                }
            }
            else{

                    if(context?.let {
                        ContextCompat.checkSelfPermission(
                            it,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        )
                    } == PackageManager.PERMISSION_GRANTED) {
                        val resolver = context?.contentResolver
                        resolver?.query(collection, PROJECTION, null, null, null)
                            ?.use { cursor ->
                                if (cursor.count > 0 && cursor.moveToFirst()) {
                                    do {
                                        val path = cursor.getStringOrNull(3)
                                        if(path != null && folder?.absolutePath?.let { path.contains(it) } == true) {
                                            val f = File(path)
                                            if(f.exists()){
                                                val duration = try {
                                                    context?.let { it1 -> f.getDuration(it1) }
                                                } catch (ex: Exception) {
                                                    null
                                                }
                                                uris.add(
                                                    Download(
                                                        cursor.getStringOrNull(1),
                                                        cursor.getStringOrNull(3),
                                                        cursor.getLongOrNull(2),
                                                        duration,
                                                        cursor.getLongOrNull(0)
                                                    )
                                                )
                                            }
                                        }
                                    }
                                    while (cursor.moveToNext())
                                    return@withContext uris
                                }
                            }
                    }



            }

            uris
        }
//    suspend fun getVideos(folder: String): ArrayList<Download?>? =
//        withContext(Dispatchers.IO) {
//            val uris : ArrayList<Download?> = ArrayList()
//            val resolver = context?.contentResolver
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                resolver?.query(collection, PROJECTION2, null, null)
//                    ?.use { cursor ->
//                        if (cursor.count > 0 && cursor.moveToFirst()) {
//                            do {
//                                val path = cursor.getStringOrNull(4)
//                                var add = false
//                                if(path?.contains(folder) == true)  add = true
//
//                                if(add)
//                                uris.add(
//                                    Download(
//                                        cursor.getStringOrNull(1),
//                                        cursor.getLongOrNull(0)?.let {
//                                            ContentUris.withAppendedId(
//                                                collection, it
//                                            )
//                                        }, cursor.getLongOrNull(2),
//                                        cursor.getLongOrNull(3),
//                                        cursor.getLongOrNull(0)
//                                    )
//                                )
//                            }
//                            while (cursor.moveToNext())
//                            return@withContext uris
//                        }
//                    }
//            }
//            else{
//                resolver?.query(collection, PROJECTION, null, null, null)
//                    ?.use { cursor ->
//                        if (cursor.count > 0 && cursor.moveToFirst()) {
//                            do {
//                                val path = cursor.getStringOrNull(3)
//                                if(path != null && path.contains(folder))
//                                uris.add(
//                                    Download(
//                                        cursor.getStringOrNull(1),
//                                        cursor.getLongOrNull(0)?.let {
//                                            ContentUris.withAppendedId(
//                                                collection, it
//                                            )
//                                        }, cursor.getLongOrNull(2),
//                                        null,
//                                        cursor.getLongOrNull(0)
//                                    )
//                                )
//                            }
//                            while (cursor.moveToNext())
//                            return@withContext uris
//                        }
//                    }
//            }
//            null
//        }


}