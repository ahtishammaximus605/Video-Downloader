package com.vid.videodownloader.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vid.videodownloader.model.Download
import java.lang.reflect.Type
import org.json.JSONArray




class StorageVideos {
    companion object{
        private var sp : SharedPreferences? = null
        private var storageEditor: SharedPreferences.Editor? = null

        fun setStorage(ctx: Context)
        {
            sp = ctx.getSharedPreferences(
                ctx.packageName + "_storage_video_downloader_videos1", Context.MODE_PRIVATE
            )
        }
//        fun getVids(): ArrayList<Download?>? {
//            val gson = Gson()
//            val json = sp?.getString("videos1", null)
//            val type: Type = object : TypeToken<ArrayList<Download?>?>() {}.type
//            return gson.fromJson(json, type)
//        }
//        fun removeVideo(value: Long) {
//            val arraylist = getVids()
//            if(arraylist !=  null && arraylist.count() > 0) {
//                var exits : Download? = null
//                arraylist.forEach {
//                    if(it?.id == value)
//                        exits = it
//                }
//                if(exits != null)
//                    arraylist.remove(exits)
//            }
//
//            storageEditor = sp?.edit()
//            val gson = Gson()
//            val json = gson.toJson(arraylist)
//            storageEditor?.putString("videos1", json)
//            storageEditor?.apply()
//            storageEditor?.commit()
//        }
//        fun setVideo(value: Download?) {
//            var arraylist = getVids()
//            if(arraylist !=  null && arraylist.count() > 0) {
//                var exits : Download? = null
//                arraylist.forEach {
//                    if(it?.id == value?.id)
//                        exits = it
//                }
//                if(exits == null)
//                arraylist.add(value)
//            }
//            else{
//                arraylist =  ArrayList<Download?>()
//                arraylist.add(value)
//            }
//
//            storageEditor = sp?.edit()
//            val gson = Gson()
//            val json = gson.toJson(arraylist)
//            storageEditor?.putString("videos1", json)
//            storageEditor?.apply()
//            storageEditor?.commit()
//        }
    }
}