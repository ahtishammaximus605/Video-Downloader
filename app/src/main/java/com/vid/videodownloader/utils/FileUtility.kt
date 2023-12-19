package com.vid.videodownloader.utils

import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import java.io.File
import java.util.*


class FileUtility {
    fun renameFile(file: File, rename: String): Boolean {
        val from = File(file.absolutePath)
        val to = File(
            file.parent,
            "$rename." + file.absolutePath.substring(file.absolutePath.lastIndexOf("."))
                .replace(".", "")
        )
        return if (to.exists()) {
            false
        } else {
            from.renameTo(to)
        }
    }
//    fun renameFile(context: Context, newName: String?, mUri: Uri): Boolean {
//        val f = File(mUri.toString())
//        return if (f.renameTo())
//            f.delete()
//        else
//            false
//    }
     fun renameUri(context: Context, newName: String?, mUri: Uri): Boolean {
        try {


            val contentValues = ContentValues()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 1)
            }
            context.contentResolver.update(mUri, contentValues, null, null)
            contentValues.clear()
            contentValues.put(MediaStore.Files.FileColumns.DISPLAY_NAME, newName)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.put(MediaStore.Files.FileColumns.IS_PENDING, 0)
            }
            context.contentResolver.update(mUri, contentValues, null, null)
            return true
        } catch (ex: java.lang.Exception) {
           Log.v("","")
        }
        return false
    }
    fun deleteFile(context: Context, uri: String): Boolean {
        val f = File(uri)
        return if (f.exists())
            f.delete()
        else
            false
//        val resolver = context.contentResolver
//        //val selectionArgsPdf = arrayOf(displayName)
//        try {
//            resolver.delete(
//                uri,null, null
//            )
//            return true
//        } catch (ex: java.lang.Exception) {
//            // show some alert message
//            Log.v("","")
//        }
//
//        return false
    }
    fun deleteFileUsingDisplayName(
        context: Context,
        id: String,
        uri: Uri?,
        path: String?
    ) {
       //val uri = getUriFromDisplayName(context, displayName)
        if (uri != null) {
            val resolver = context.contentResolver
            val selectionArgsPdf = arrayOf(id)
            try {
                resolver.delete(
                    uri,
                    MediaStore.Video.Media._ID + "=?",
                    selectionArgsPdf
                )
                path?.let { it1 -> FileUtility().deleteFile(context, it1) }

            } catch (ex: java.lang.Exception) {

            }
        }
        //return false
    }
    private fun getUriFromDisplayName(
        context: Context,
        displayName: String
    ): Uri? {
        val extUri: Uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = arrayOf(MediaStore.Files.FileColumns._ID)

        val cursor = context.contentResolver.query(
            extUri,
            projection,
            MediaStore.Video.Media.TITLE + " LIKE ?",
            arrayOf(displayName),
            null
        )!!
        cursor.moveToFirst()
        return if (cursor.count > 0) {
            val columnIndex = cursor.getColumnIndex(projection[0])
            val fileId = cursor.getLong(columnIndex)
            cursor.close()
            Uri.parse(extUri.toString().toString() + "/" + fileId)
        } else {
            null
        }
    }
    fun getSIzeMB(fileSizeInBytes: Long): String {
        val fileSizeInKB = fileSizeInBytes / 1024
        return if (fileSizeInKB > 1024){
            // Convert the KB to MegaBytes (1 MB = 1024 KBytes)
            val fileSizeInMB = fileSizeInKB / 1024
            fileSizeInMB.toString() + "MB"
        }else{
            fileSizeInKB.toString() + "KB"
        }

    }
    fun getSIzeFromDuration(res :Int, time: Long): Long {
        return when (res) {
            480 -> res + time + 1508
            720 -> res + time + 4340
            else -> 0
        }
    }
    companion object{
        private val File.uri get() = this.absolutePath.asUri()
        private fun String?.asUri(): Uri? {
            try {
                return Uri.parse(this)
            } catch (e: Exception) {
            }
            return null
        }
        fun File.getSize(): String {
            // Get length of file in bytes
            val fileSizeInBytes: Long = this.length()
            return FileUtility().getSIzeMB(fileSizeInBytes)
        }
        fun Context?.getDuration(uri : Uri?): Long {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this,uri)
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                duration?.toLongOrNull() ?: 0
            } catch (ex: RuntimeException){
                0
            }
        }
        fun String.getDuration(): Long {
            return try {
                val retriever = MediaMetadataRetriever()
                retriever.setDataSource(this, HashMap<String, String>())
                val duration =
                    retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                duration?.toLongOrNull() ?: 0
            } catch (ex: RuntimeException){
                0
            }
        }
        fun File.getDuration(context: Context): Long {
            if (!exists()) return 0
            return try {
                val retriever =  MediaMetadataRetriever()
                retriever.setDataSource(context,uri)
                val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                retriever.release()
                return duration?.toLongOrNull() ?: 0
            } catch (ex: RuntimeException){
                0
            }
        }


        fun File.getDurationString(context: Context): String {
            val duration = getDuration(context)
            val c: Calendar = Calendar.getInstance()
            c.timeInMillis = duration
            val hour: Int = c.get(Calendar.HOUR_OF_DAY)
            val minute: Int = c.get(Calendar.MINUTE)
            var times = "" + hour
            times += if (minute < 10) {
                ":0$minute"
            } else {
                ":$minute"
            }
            return times
        }
        fun Long.getDurationString(): String {
            val minutes = this / 1000 / 60
            val seconds = this / 1000 % 60
            val secondsStr = seconds.toString()
            val secs: String = if (secondsStr.length >= 2) {
                secondsStr.substring(0, 2)
            } else {
                "0$secondsStr"
            }
            return "$minutes:$secs"
        }
        fun String.getDurationString(): String {
            return getDuration().getDurationString()
        }
        fun Context.isPackageInstalled(packageName: String): Boolean {
            return try {
                packageManager.getPackageInfo(packageName, 0)
                true
            } catch (e: PackageManager.NameNotFoundException) {
                false
            }

        }
    }


}