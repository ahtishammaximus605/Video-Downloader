package com.vid.videodownloader.background

import android.util.Log
import androidx.annotation.WorkerThread
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.utils.FileUtility
import kotlinx.coroutines.*
import org.json.JSONObject
import org.jsoup.Connection
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URL


class ExtractLink
{

    @WorkerThread
    suspend fun getLinks(url : String): Resource<String?>?  {

        var pData: Resource<String?>? = null
        var translatedText = ""
        withContext(Dispatchers.IO) {
            try {

                val response: Connection.Response = Jsoup.connect("https://fdown.net/download.php")
                    .userAgent("Mozilla/5.0")
                    .timeout(30 * 1000)
                    .method(Connection.Method.POST)
                    .data("URLz", url)
                    .followRedirects(true)
                    .execute()

                //parse the document from response
                val doc: Document = response.parse()

                //val mapCookies: Map<String, String> = response.cookies()
                var name = doc.getElementsByClass("lib-header").text()
                var sdlink = doc.getElementById("sdlink")?.attr("href")
                var hdlink = doc.getElementById("hdlink")?.attr("href")
                var icon = doc.getElementsByClass("lib-img-show").attr("src")
                if(name == "null" || name == null) name = ""
                if(icon == "null" || icon == null) icon = ""
                if(sdlink == "null" || sdlink == null) sdlink = ""
                if(hdlink == "null" || hdlink == null) hdlink = ""
                val links = name + "_blankspace101_" + icon +"_blankspace101_"+sdlink+"_blankspace101_"+hdlink

                pData = Resource<String?>(Resource.Status.SUCCESS, links, "")
            } catch (ex: HttpStatusException) {
                pData =  Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: MalformedURLException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: SocketTimeoutException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: IOException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: Exception) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
        }
        return pData
    }

    @WorkerThread
    suspend fun getFileSize(url : String): Resource<String?>? {

        var pData: Resource<String?>? = null
        withContext(Dispatchers.IO) {
            try {
                val myUrl = URL(url)
                val myConnection = myUrl.openConnection()
                val headersize: MutableList<String>? = myConnection.headerFields["content-Length"]
                val lenghtOfFile: Long? = headersize?.get(0)?.toLongOrNull()
                if(lenghtOfFile != null )
                    pData = Resource<String?>(Resource.Status.SUCCESS, FileUtility().getSIzeMB(lenghtOfFile.toLong()), "")
                else  pData = Resource<String?>(Resource.Status.SUCCESS, "UNKNOWN", "")
            }
            catch (ex: HttpStatusException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: MalformedURLException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: SocketTimeoutException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: IOException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
            catch (ex: Exception) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
            }
        }
        return pData
    }




}