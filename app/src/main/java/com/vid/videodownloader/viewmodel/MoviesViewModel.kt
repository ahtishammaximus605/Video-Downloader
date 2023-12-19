package com.vid.videodownloader.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vid.videodownloader.background.ExtractLink
import com.vid.videodownloader.background.MovieRepository
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.model.VideoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch


class MoviesViewModel : ViewModel()
{

    private var el: MovieRepository? = null
    companion object{

    }
    var videos: MutableLiveData<ArrayList<VideoModel>?>? = MutableLiveData()

    fun init() {

        el = MovieRepository()
    }
    fun getMoviesAsync(url : String)= viewModelScope.launch(Dispatchers.IO) {
        videos?.postValue(el?.getLatestMovies(null))
    }
    suspend fun getVideoId(videoId : Long) = viewModelScope.async(Dispatchers.IO) {
        el?.getVideoId(videoId)
    }
}