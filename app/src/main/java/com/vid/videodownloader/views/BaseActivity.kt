package com.vid.videodownloader.views

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.res.Resources
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ParseException
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.webkit.URLUtil
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.WorkerThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.tonyodev.fetch2.NetworkType
import com.tonyodev.fetch2.Priority
import com.tonyodev.fetch2.Request
import com.vid.videodownloader.MyApp
import com.vid.videodownloader.background.ExtractLink
import com.vid.videodownloader.interfaces.ApiService
import com.vid.videodownloader.interfaces.LinkApiService
import com.vid.videodownloader.model.LinkVideoData
import com.vid.videodownloader.model.RemoteIds
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.model.VideoData
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.viewmodel.DownloadViewModel
import com.vid.videodownloader.views.DownloadLinkActivity.Companion.errorDownloading
import com.vid.videodownloader.views.dialogs.DownloadProgressDialog
import com.vid.videodownloader.views.dialogs.PremiumDialog
import com.vid.videodownloader.views.dialogs.dialogInterface
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.jsoup.HttpStatusException
import org.jsoup.Jsoup
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Random


abstract class BaseActivity<B : ViewBinding>(val bindingFactory: (LayoutInflater) -> B) : AppCompatActivity() {
    lateinit var binding: B

    var adLyt1 : ConstraintLayout? = null
    var adProgressBar1: ProgressBar? = null
    var adContent1: LinearLayout? = null

    var adLyt : ConstraintLayout? = null
    var adProgressBar: ProgressBar? = null
    var adContent: FrameLayout? = null
    var navigationContent: FragmentContainerView? = null
    var type = 1
    var isActiveBase = true
    var isProgress = false
    private val mViewModel : DownloadViewModel by viewModels()
    var clipboard : ClipboardManager? = null
    var isCreated = false
    var lytInterProgress : ConstraintLayout? = null


    private val BASE_URL = "https://thinkshot.site/"

    // URL and KEY values from the provided URL
    val requestMap = mapOf(
        "URL" to "https://www.facebook.com/watch/?v=1419856788851776",
        "KEY" to "1234"
    )


    private var apiService: ApiService? = null
    private var linkApiService: LinkApiService? = null
   // var adLoader : AdLoader? = null

   companion object {
       var currentAct = ""
       var admobId = StorageSharedPref.get(RemoteIds.native_id) ?: ""
       private var timer: Handler? = null
       var remoteCurrentAdKey: String? = null
       var isAdClicked = false
       var isOutOffApp = false
       var retrofit: Retrofit? = null
       var retrofitLink: Retrofit? = null
       var playerActivityBack = false
   }
    var wasPasted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("AppState",""+lifecycle.currentState)
        super.onCreate(savedInstanceState)
        try {
            binding = bindingFactory(layoutInflater)
            setContentView(binding.root)
        } catch (ex: Resources.NotFoundException){}

        mViewModel.init()
        Log.v("AppState",""+lifecycle.currentState)
    }

    override fun onResume() {
        Log.v("AppState",""+lifecycle.currentState)
        super.onResume()
        Log.v("AppState",""+lifecycle.currentState)

        isActiveBase = true
        isOutOffApp = false

    }
    override fun onPause() {
        Log.v("AppState",""+lifecycle.currentState)
        super.onPause()
        Log.v("AppState",""+lifecycle.currentState)
        isActiveBase = false
        wasPasted = false
    }
    var dialogDown : DownloadProgressDialog? = null
    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.v("AppState",""+lifecycle.currentState)
        super.onWindowFocusChanged(hasFocus)
//        val autoCopy =  StorageSharedPref.get("auto_copy")
//        if(autoCopy != "0")
       // if (hasFocus && !wasPasted) autoCopy(DownloadLinkActivity().binding.editTextTextPersonName)
        Log.v("AppState",""+lifecycle.currentState)
    }


     fun autoCopy(editTextTextPersonName: AppCompatEditText)
    {
//        if (this !is SplashActivity && this !is PremiumActivity && this !is DashboardActivity && this !is PermissionActivity) {
//                var hasNoLink = false
//                try {
//                        clipboard =
//                            getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
//                        if (clipboard != null) {
//                            if (clipboard!!.hasPrimaryClip() && clipboard!!.primaryClip != null) {
//                                val s = clipboard!!.primaryClip!!.getItemAt(1).coerceToText(this)
//                                if (s != null) {
//                                    val url = s.toString()
//                                    Log.v("clip", "" + url)
//                                    if (URLUtil.isValidUrl(url)) {
//                                        Log.v("URLUtil", "" + url)
//                                            if (StorageSharedPref.get("copied") != url)
//                                            {
//                                                //StorageSharedPref.save("copied", "")
//                                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//                                                    try {
//                                                        Log.v("SDK_INT", "")
//                                                        val clipData =
//                                                            ClipData.newPlainText("", "")
//                                                        if (clipData != null)
//                                                            clipboard?.setPrimaryClip(clipData)
//
//                                                        clipboard?.clearPrimaryClip()
//
//                                                    } catch (ex: NullPointerException) {
//
//                                                    }
//                                                    catch (ex: Exception) {
//
//                                                    }
//                                                } else {
//                                                    try {
//                                                        val clipData =
//                                                            ClipData.newPlainText("", "")
//                                                        clipboard!!.setPrimaryClip(clipData)
//                                                    } catch (ex: NullPointerException) {
//
//                                                    }
//                                                    catch (ex: Exception) {
//
//                                                    }
//                                                }
//                                                editTextTextPersonName.setText(url)
//                                            }
//                                            else hasNoLink = true
//
//                                    }
//                                    else hasNoLink = true
//                                }
//                                else hasNoLink = true
//                            }
//                        } else hasNoLink = true
//
//                } catch (ex: IllegalStateException) {
//                }
//                catch (ex: Exception) {
//
//                }
//
//        }
    }

    private fun getDataFromNetwork(url1: String,listenerFromProcess: () -> Unit) {
        if(url1.contains("facebook") || url1.contains("fb.watch")|| url1.contains("dailymotion"))
        {
            getAnythingResponse(url1) {
                listenerFromProcess.invoke()
            }

        }
        else{
            getAnythingResponse(url1) {
                listenerFromProcess.invoke()
            }
        }

    }

    private fun getAnythingResponse(url1: String,listenerFrom: () -> Unit) {
        try {
            val apiService = retrofit?.create(ApiService::class.java)

            val call = apiService?.postData(url1, "1234")

            call?.enqueue(object : Callback<List<VideoData>> {
                override fun onResponse(
                    call: Call<List<VideoData>>,
                    response: Response<List<VideoData>>
                ) {
                    if (response.isSuccessful) {
                        Log.e("onResponse", "isSuccessful: $response")
                        val dataList = response.body()

                        val videoDataList = response.body()
                        if (videoDataList != null && videoDataList.isNotEmpty()) {
                            val videoData = videoDataList[0]
                            val src = videoData.quality
                            val urlSd = videoData.url
                            if (videoDataList.size > 1) {

                                val videoDataHd = videoDataList[1]
                                val srchd = videoDataHd.quality
                                val urlHd = videoDataHd.url
                                listenerFrom.invoke()
                                startDownload(urlSd,url1)
                            }

                        } else {
                           // dialogDown?.dismissAllowingStateLoss()
                            listenerFrom.invoke()
                            Toast.makeText(
                                this@BaseActivity,
                                "Video data not found!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } else {
                        //if didit get responce
                    }
                }

                override fun onFailure(call: Call<List<VideoData>>, t: Throwable) {
                    Log.e("onResponse", "onFailure: $t")
                  //  dialogDown?.dismiss()

                    getLinkResponse(url1) { listenerFrom.invoke() }
                    Toast.makeText(
                        this@BaseActivity,
                        "Hold on we are fetching Link",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
        } catch (ex: IOException) {
            listenerFrom.invoke()
            Toast.makeText(
                this@BaseActivity,
                "Slow Network! Try Downloading on a faster network.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getLinkResponse(url1: String,listenerFrom: () -> Unit) {
        try {
            val apiService = retrofitLink?.create(LinkApiService::class.java)

            val call = apiService?.postData(url1, 0)
            call?.enqueue(object : Callback<LinkVideoData> {
                override fun onResponse(
                    call: Call<LinkVideoData>,
                    response: Response<LinkVideoData>
                ) {
                    if (response.isSuccessful) {
                        Log.e("onResponse", "isSuccessful: $response")
                        val videoData = response.body()

                        if (videoData != null) {
                            dialogDown?.nameUrl = videoData.title
                            dialogDown?.sdLink = videoData.formats[0].url
                            if (videoData.formats.size > 1) {

                                listenerFrom.invoke()
                                startDownload(videoData.formats[1].url,url1)
                            }

                            dialogDown?.dataUpdated?.postValue(1)
                        } else {
                            listenerFrom.invoke()
                            Toast.makeText(
                                this@BaseActivity,
                                "Video data not found!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        // Handle unsuccessful response here
                    }
                }

                override fun onFailure(call: Call<LinkVideoData>, t: Throwable) {
                    listenerFrom.invoke()
                    Log.e("onResponse", "onFailure: $t")
                    errorDownloading?.invoke("Error!","Video Not Available...")
                    Toast.makeText(
                        this@BaseActivity,
                        "Can't Process link this time",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            })
        } catch (ex: IOException) {
            listenerFrom.invoke()
            errorDownloading?.invoke("Error!","Video Not Available...")
            Toast.makeText(
                this@BaseActivity,
                "Slow Network! Try Downloading on a faster network.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun generateRandomNumber(): String {
        val random = Random()
        val sb = StringBuilder(20)

        for (i in 0 until 20) {
            val digit = random.nextInt(10)
            sb.append(digit)
        }

        return sb.toString()
    }
    fun processVideo(vidData: String?,listenerFromProcessVid: () -> Unit) {
        try {
            if(vidData?.contains("https://www.youtube.com/") == true || vidData?.contains("https://youtu.be/")== true
                ||vidData?.contains("https://youtube.com/")== true ||vidData?.contains("https://music.youtube.com/watch")== true)
            {

                errorDownloading?.invoke("Restricted Content","Download videos from youtube is currently not supported in \"Video Downloader\"")
                Toast.makeText(this, "Cannot process this Url.", Toast.LENGTH_SHORT).show()
                listenerFromProcessVid.invoke()

            }
            else
            { StorageSharedPref.save("copied", "")
                wasPasted = true
                lifecycleScope.launch {
                    Log.e("testing", "By pass youtube check : ", )
                    getDataFromNetwork(vidData!!) {
                        listenerFromProcessVid.invoke()
                    }

                }

              }

        } catch (e: Exception) {}
    }

    fun getVideo(whole : String): Resource<String?>? {
        var pData: Resource<String?>? = null
        var json: String? = null
        try {
            val document = Jsoup.parse(whole.toString())
            val el4 = document.select("div[data-store*=videoID]")
            if (el4.count() > 0) {
                json = el4[0]?.attr("data-store")
                pData = Resource<String?>(Resource.Status.SUCCESS, json, "")
            }else{
                pData = Resource<String?>(Resource.Status.ERROR, "Video Not Found!", "")
            }

        } catch (ex: HttpStatusException) {
            pData =  Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: MalformedURLException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }
        catch (ex: IllegalArgumentException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }
        catch (ex: SocketTimeoutException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: IOException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: Exception) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }

        return pData
    }

    private var el: ExtractLink? = null
    private suspend fun downloadUrl(url: String) = lifecycleScope.async(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        return@async try {
            val form = URL(url)
            connection = (form.openConnection() as? HttpURLConnection)
            connection?.run {
                readTimeout = 5000
                connectTimeout = 4000
                doInput = true
                connect()
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    throw IOException("HTTP error code: $responseCode")
                }
                inputStream?.let { stream ->
                    readStream(stream)
                }
            }
        } finally {
            // Close Stream and disconnect HTTPS connection.
            connection?.inputStream?.close()
            connection?.disconnect()
        }
    }.await()
    /**
     * Converts the contents of an InputStream to a String.
     */
    private fun readStream(stream: InputStream): String? {
        val whole = StringBuilder()
        val input = BufferedReader(
            InputStreamReader(BufferedInputStream(stream))
        )
        var inputLine: String?
        while (input.readLine().also { inputLine = it } != null) whole.append(inputLine)

        return whole.toString()
    }


    @WorkerThread
    suspend fun getVideoSrc(url : String)= lifecycleScope.async(Dispatchers.IO){
        var pData: Resource<String?>? = null
        var json: String? = null
        var connection1: HttpURLConnection? = null
        try {
            val form = URL(url)
             connection1 = form.openConnection() as HttpURLConnection
            //connection1.setRequestProperty("Cookie", "https://m.facebook.com")
            connection1.connectTimeout = 5000
            connection1.readTimeout = 5000
            if(connection1.responseCode == HttpURLConnection.HTTP_OK) {
                val whole = StringBuilder()
                val input = BufferedReader(
                    InputStreamReader(BufferedInputStream(connection1.inputStream))
                )
                var inputLine: String?
                while (input.readLine().also { inputLine = it } != null) whole.append(inputLine)
                input.close()
                connection1.disconnect()
                val document = Jsoup.parse(whole.toString())
                val el4 = document.select("div[data-store*=videoID]")

                if (el4.count() > 0) {
                    json = el4[0]?.attr("data-store")
                    pData = Resource<String?>(Resource.Status.SUCCESS, json, "")
                }else{
                    pData = Resource<String?>(Resource.Status.ERROR, "Video Not Found!", "")
                }
            } else pData = Resource<String?>(Resource.Status.ERROR, "Video Not Found!", "")

        } catch (ex: HttpStatusException) {
            pData =  Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: MalformedURLException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }
        catch (ex: IllegalArgumentException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }
        catch (ex: SocketTimeoutException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: IOException) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        } catch (ex: Exception) {
            pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
        }
//        Handler(Looper.myLooper()!!).postDelayed({
//            if(connection1?.responseCode != HttpURLConnection.HTTP_OK) {
//                connection1?.disconnect()
//            }
//        }, 6000)

        return@async pData
    }.await()
    // Extension method to convert pixels to dp
    fun Int.toDp(context: Context):Int = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,this.toFloat(),context.resources.displayMetrics
    ).toInt()

    override fun onStart() {
        Log.v("AppState",""+lifecycle.currentState)
        super.onStart()
        Log.v("VIDEODOWNLOADER","base start")
        currentAct = localClassName
        if(!StorageSharedPref.isNetworkAvailable(applicationContext)
            || !StorageSharedPref.verifyInstallerId(applicationContext)
            || StorageSharedPref.isAppPurchased() == true)
        {
            adLyt?.visibility = View.GONE
        }
        Log.v("AppState",""+lifecycle.currentState)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        Log.v("AppState",""+lifecycle.currentState)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


    fun startActivityDestroyNativeAd(intentP : Intent)
    {
        startActivity(intentP)
    }
    fun finishActivityWithAdCheck()
    {
        finish()
    }

    var adStatus = "no"


    fun onAdFailed() {
        adStatus = "failed"
            adLyt?.visibility = View.GONE
        adContent?.visibility = View.GONE
    }

    fun onAdReloadStarted() {
        adStatus = "reload"
            if (StorageSharedPref.isAppPurchased() != true) {
                isAdClicked = true
                adLyt?.visibility = View.VISIBLE
                adProgressBar?.visibility = View.VISIBLE
                adContent?.visibility = View.GONE
            }

    }
    fun startNewActivity(intent1 : Intent)
    {
        startActivity(intent1)
    }
    fun startNewActivityFinishPrevious(intent1 : Intent)
    {
        startActivity(intent1)
        finish()
    }
    var nextIntent :Intent? = null
    var isFinish : Boolean = true
    open val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if(nextIntent != null)
            startActivity(nextIntent)
            //if(isFinish)
            //finish()
        }
    }
    open fun startPremiumAd(intent1: Intent?, finish : Boolean = true){
        isFinish = finish
        nextIntent = intent1
        val dialog = PremiumDialog(this)
        dialog.window?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.dg = object : dialogInterface {
            override fun onClose() {
                if(application is MyApp)
                (application as MyApp).purchase(this@BaseActivity)
            }
        }
        dialog.setOnDismissListener {
            if (nextIntent != null) {
                startActivity(nextIntent)
            } else finish()

        }

        val window: Window? = dialog.window
        window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
    }
    fun startAdActivity(intent: Intent, remote : String, activity : Activity?){
        activity?.let { it1 ->
            activity.startActivity(intent)
            activity.finish()

        }

    }


    private fun startDownload(url : String?,nameUrl:String){

        if(url != null && url != "" && url != "null") {
            try {
                val folder = this?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                val mBaseFolderPath: String = folder.toString() + "/" + "videodownloader"
                if (!File(mBaseFolderPath).exists()) {
                    File(mBaseFolderPath).mkdir()
                }
                val filePath: String = folder.toString() + "/videodownloader/" + getRandomFileName("mp4")

                val request = Request(url, filePath)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.tag = nameUrl
                request.groupId = groupId

                // val it  = StorageVideos.getVids()
                val exists = DownloadService.downloadsList?.find { s -> s.tag == nameUrl }
                if (exists == null) {
                    if (DownloadService.rxFetch?.isClosed == true || DownloadService.rxFetch == null) {
                        this?.stopService(Intent(this, DownloadService::class.java))
                        this?.startService(Intent(this, DownloadService::class.java))
                    }

                    Toast.makeText(this, "Starting Download", Toast.LENGTH_LONG).show()

                    DownloadService.rxFetch?.enqueue(request, {
                        DownloadService.vidCount++
                        DownloadService.videoDownloadStartedLiveData.postValue("1")
                    })
                    {
                        Log.e("downloadUrl", "Error: ${it}",)
                        //   Toast.makeText(this, "Video Cant Be Downloaded!", Toast.LENGTH_LONG).show()
                    }
                } else
                {

                    Toast.makeText(this, "Video Already downloaded!", Toast.LENGTH_LONG).show()
                    DownloadLinkActivity.videoAlready?.invoke()

            }

            }
            catch(ex: IllegalArgumentException ){
                Toast.makeText(this, "Not Available! "+ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: URISyntaxException){
                Toast.makeText(this, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: ParseException){
                Toast.makeText(this, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: Exception ){
                Toast.makeText(this, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
        }



    }
    fun getRandomFileName(fileExtension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val randomString = (1..6)
            .map { kotlin.random.Random.nextInt(0, 36) }
            .map { if (it < 10) it.toString() else ('a' + it - 10).toString() }
            .joinToString("")

        return "vid_downloader_$timestamp$randomString.$fileExtension"
    }

}