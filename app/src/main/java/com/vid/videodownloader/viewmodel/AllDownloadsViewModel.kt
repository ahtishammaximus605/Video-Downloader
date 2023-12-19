package com.vid.videodownloader.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tonyodev.fetch2.Fetch
import com.tonyodev.fetch2.FetchConfiguration
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import com.vid.videodownloader.model.Resource
import kotlinx.coroutines.async

class AllDownloadsViewModel : ViewModel(){
    var downloads: MutableLiveData<List<com.tonyodev.fetch2.Download>>? = null
    suspend fun getUrlsAsync(url : String)= viewModelScope.async {

    }
}