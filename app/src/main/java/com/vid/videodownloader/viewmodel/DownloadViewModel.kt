package com.vid.videodownloader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vid.videodownloader.background.ExtractLink
import com.vid.videodownloader.model.Resource
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class DownloadViewModel : ViewModel()
{

    private var mutableLiveData: MutableLiveData<Resource<String?>>? = null
    private var el: ExtractLink? = null

    fun init() {

        if (mutableLiveData != null) {
            return
        }

        el = ExtractLink()

    }
    suspend fun getUrlsAsync(url : String)= viewModelScope.async {
        el?.getLinks(url)
    }
    suspend fun getUrlFileSize(url :String)=
        withContext(viewModelScope.coroutineContext) {
            el?.getFileSize(url)
        }

}