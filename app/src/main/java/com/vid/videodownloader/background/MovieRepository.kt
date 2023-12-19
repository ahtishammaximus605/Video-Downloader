package com.vid.videodownloader.background


import android.util.Log
import com.vid.videodownloader.model.VideoModel
import com.vid.videodownloader.utils.Constants.ACTION
import com.vid.videodownloader.utils.Constants.ADVENTURE
import com.vid.videodownloader.utils.Constants.ANIMATION
import com.vid.videodownloader.utils.Constants.COMEDY
import com.vid.videodownloader.utils.Constants.CRIME
import com.vid.videodownloader.utils.Constants.DRAMA
import com.vid.videodownloader.utils.Constants.HORROR
import com.vid.videodownloader.utils.Constants.ROMANCE
import com.vid.videodownloader.utils.Constants.SCI_FI
import com.vid.videodownloader.utils.Constants.TRENDING
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

const val apikey = "3a7d44cc559ce6e3ee8da88c34b5a7f2"
class MovieRepository {
    suspend fun getLatestMovies(searchText: String? = null) : ArrayList<VideoModel>  {
        val videos: ArrayList<VideoModel> = ArrayList()
         withContext(Dispatchers.IO) {
             try {
                 val url = "https://api.themoviedb.org/3/discover/movie?api_key=$apikey&language=en-US&sort_by=popularity.desc&include_adult=false&include_video=true&page=1&with_genres=${getRandomGenre()}"

                 val client = OkHttpClient.Builder()
                     .connectTimeout(5, TimeUnit.SECONDS)
                     .writeTimeout(5, TimeUnit.SECONDS)
                     .readTimeout(5, TimeUnit.SECONDS)
                     .callTimeout(10, TimeUnit.SECONDS)
                     .build()
                 val request = Request.Builder()
                     .url("\n" + url)
                     .get()
                     .build()

                 val response = client.newCall(request).execute()
                 val jsonData: String = response.body?.string()!!
                 val Jobject = JSONObject(jsonData)
                 val Jarray = Jobject.getJSONArray("results")

                 response.close()

                 if (Jarray.length() > 0) {
                     for (i in 0 until Jarray.length()) {
                         val movie = Jarray.getJSONObject(i)
                         if (movie != null) {
                             val posterPath: String? =
                                 if (movie.get("poster_path")
                                         .toString() == "null" || movie.get("poster_path")
                                         .toString().isEmpty()
                                 ) {
                                     null
                                 } else {
                                     "https://image.tmdb.org/t/p/original/" + movie.get("poster_path")
                                         .toString()
                                 }

                             val id = movie.get("id").toString()

                             val title = movie.get("title").toString()

                             val videoModel = VideoModel()
                             videoModel.videoId = id
                             videoModel.videoTitle = title
                             videoModel.videoUri = posterPath
                             videos.add(videoModel)
                         }


                     }
                 }
                 else videos


             } catch (ex: Exception) {
                 Log.e("TrendingFragment", "Exception: getLatestMovies(), " + ex.message)
             }
         }
         return videos
    }
     suspend fun getVideoId(videoId : Long) : String{
         var videoYid = ""
         withContext(Dispatchers.IO) {
             try {

                 val client2 = OkHttpClient.Builder()
                     .connectTimeout(5, TimeUnit.SECONDS)
                     .writeTimeout(5, TimeUnit.SECONDS)
                     .readTimeout(5, TimeUnit.SECONDS)
                     .callTimeout(10, TimeUnit.SECONDS)
                     .build()
                 val request2 = Request.Builder()
                     .url("https://api.themoviedb.org/3/movie/${videoId}/videos?api_key=$apikey&language=en-US")
                     .get()
                     .build()

                 val response2 = client2.newCall(request2).execute()
                 val jsonData2: String = response2.body?.string()!!
                 val Jobject2 = JSONObject(jsonData2)

                 val result: JSONArray = Jobject2["results"] as JSONArray
                 if (result.length() > 0) {
                     val jo = result.getJSONObject(0)
                     videoYid = jo["key"].toString()
                 }

                 response2.body?.close()


             } catch (ex: Exception) {
                 Log.e("TrendingFragment", "Exception: getLatestMovieDetails(), " + ex.message)
             }
         }
        return videoYid
    }

    fun getRandomGenre(): Int {
        val genres = listOf(TRENDING, ACTION, ROMANCE, COMEDY, HORROR, DRAMA, ANIMATION, ADVENTURE, CRIME, SCI_FI)
        return genres.random()
    }

    // Usage example
    fun main() {
        val randomGenre = getRandomGenre()
        println("Randomly selected genre: $randomGenre")
    }

}