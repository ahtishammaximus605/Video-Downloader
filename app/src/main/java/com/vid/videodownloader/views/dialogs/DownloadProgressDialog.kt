package com.vid.videodownloader.views.dialogs

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.DownloadProgressBinding
import com.vid.videodownloader.interfaces.OnDownloadClick
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.FileUtility
import com.vid.videodownloader.utils.FileUtility.Companion.getDurationString
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.DownloadLinkActivity
import com.vid.videodownloader.views.PlayerActivity
import com.vid.videodownloader.views.groupId
import kotlinx.coroutines.*
import org.jsoup.HttpStatusException
import java.io.File
import java.io.IOException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URISyntaxException
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class DownloadProgressDialog: BottomSheetDialogFragment()//(private var context?: Activity) : Dialog(context?,R.style.Theme_App_Dialog_FullScreen)
{
        private lateinit var binding: DownloadProgressBinding
    var downloadClickListener: OnDownloadClick? = null
        var name: String? = null
        var sdLink : String?  = null
        var hdLink : String?  = null
        var url : String? = ""
        var watchUrl: String? = ""
        var nameUrl :String? = ""
        var newName = ""
        var sSize = "0 MB"

    var dataUpdated : MutableLiveData<Int?> = MutableLiveData()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DownloadProgressBinding.inflate(layoutInflater)
        dialog?.setCancelable(true)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lytProgress.visibility = View.VISIBLE
        initDownloader()
        dataUpdated.observe(this, Observer {
            if (it == 1) {
                binding.lytProgress.visibility = View.GONE
                if (nameUrl?.endsWith("/") == true) {
                    nameUrl = nameUrl?.substring(0, nameUrl!!.length - 1)
                }
                val sName = nameUrl?.split("/")

                newName = sName?.get(sName.size - 1) ?: ""
                name = newName
                Log.v("DownloadProgressDialog", "onCreate" + newName)
                binding.txtName.text = newName

                watchUrl?.let { it1 -> select(it1, 2) }
                if (name.isNullOrEmpty() && watchUrl.isNullOrEmpty()) {
                    binding.txtName.text = "Video Not Available"
                    binding.btnDownload.isEnabled = false
                    binding.btnPlay.isEnabled = false
                    binding.btnPlay.visibility = View.INVISIBLE
                    disableLayouts()
                }
                if (hdLink.isNullOrEmpty() && !URLUtil.isValidUrl(hdLink)) {
                    disableHdLayouts()
                }
                if (sdLink != null && URLUtil.isValidUrl(sdLink))
                    sdLink?.let { it1 -> select(it1, 2) }

                if (watchUrl == null || watchUrl == "") dismiss()
                else {
                    longRunningTasks()
                    if (context?.let { StorageSharedPref.isNetworkAvailable(it) } != true) {
                        Toast.makeText(context, "No Internet!", Toast.LENGTH_LONG).show()
                    }
                }
            }

        })
        binding.btnClose.setOnClickListener {
            binding.lytProgress.visibility = View.GONE
            dismiss()
        }
        binding.btnSd.setOnClickListener {
            if(sdLink != null && sdLink != "" && sdLink != "null")
                sdLink?.let { it1 -> select(it1, 2) }
            else if(watchUrl != null && watchUrl != "")
                watchUrl?.let { it1 -> select(it1, 2) }
            else   Toast.makeText(context, "SD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding.btnHd.setOnClickListener {
            if(hdLink != null && hdLink != "" && hdLink != "null")
                hdLink?.let { it1 -> select(it1, 1) }
            else   Toast.makeText(context, "HD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding.btnDownload.setOnClickListener {
            DownloadLinkActivity.downloadListener={
                if(url == null || url == "" || url == "null")
                    url = sdLink
                if(url != null && url != "" && url != "null") {
                    downloadUrl(url)
                }
                else   Toast.makeText(context, "Video Not Found.", Toast.LENGTH_LONG).show()
            }
            downloadClickListener?.onDownloadClick()
            {
                if(url == null || url == "" || url == "null")
                    url = sdLink
                if(url != null && url != "" && url != "null") {
                    downloadUrl(url)
                }
                else   Toast.makeText(context, "Video Not Found.", Toast.LENGTH_LONG).show()
            }


        }
        binding.btnPlay.setOnClickListener {

            if(sdLink != null && sdLink != "")
                url= sdLink
            else if(watchUrl != null && watchUrl != "")
                url= watchUrl

            if(watchUrl != null && watchUrl != "")
            {
                itemClickResult(watchUrl!!,"")
            }else dismissAllowingStateLoss()


        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("watchUrl")?.let {
            watchUrl = it
        }
        arguments?.getString("nameUrl")?.let {
            nameUrl = it
        }
//        if (context is ItemClickListener) {
//            mListener = context as ItemClickListener
//        } else {
//            throw RuntimeException(
//                context.toString()
//                    .toString() + " must implement ItemClickListener"
//            )
//        }

    }

    override fun onDetach() {
        super.onDetach()
        //mListener = null
    }
//    interface ItemClickListener {
//        fun onItemClick(item: String)
//    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): DownloadProgressDialog {
            val fragment = DownloadProgressDialog()
            fragment.arguments = bundle
            return fragment
        }
    }

    private fun itemClickResult(w: String, name: String) {
        context?.let {

                        binding.lytProgress.visibility = View.GONE
                        context?.startActivity(
                            Intent(
                                context,
                                PlayerActivity::class.java
                            ).putExtra("url", w)
                        )
        }

    }
    var job : Job? = null
    fun longRunningTasks(){
        binding.lytProgress.visibility = View.VISIBLE
        if(!context?.let { StorageSharedPref.isNetworkAvailable(it) }!!){
            Toast.makeText(context, "No Internet!", Toast.LENGTH_LONG).show()
        }
        if(watchUrl != null && watchUrl != "") {
            binding.btnDownload.isEnabled = true

        }

        Log.v("DownloadProgressDialog", "onCreate"+sdLink)
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            if(isActive) {
                try {
                    if (watchUrl != null && watchUrl != "") {
                        var image: Bitmap? = null
                        var s : Long = 0
                        try {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(watchUrl, HashMap())
                            image =
                                retriever.getFrameAtTime(
                                    2000000,
                                    MediaMetadataRetriever.OPTION_CLOSEST_SYNC
                                )
                            val d =
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                            retriever.release()
                            s = d?.toLongOrNull() ?: 0
                        } catch (ex: java.lang.IllegalArgumentException) {

                        } catch (ex: java.lang.RuntimeException) {

                        }
                        catch (ex: NoSuchMethodError) {

                        }

                        val time = s.getDurationString()
                        sSize = FileUtility().getSIzeMB(FileUtility().getSIzeFromDuration(480, s))
                        val size2 = getFileSize(watchUrl!!)
                        withContext(Dispatchers.Main) {
                            binding?.lytProgress?.visibility = View.GONE
                            binding?.txtsSize?.text = sSize
                            binding?.txtSize?.text = sSize
                            binding?.txtDuration?.text = time
                            if (image != null)

                                binding?.imgIcon?.let { it1 ->
                                    Glide.with(requireContext()).load(image)
                                        .placeholder(R.drawable.download_ic)
                                        .disallowHardwareConfig()
                                        .error(R.drawable.download_ic)
                                        .into(it1)
                                }

                            if (size2 != null) {
                                if (size2.status == Resource.Status.SUCCESS) {
                                    if (size2.data != "UNKNOWN") {
                                        binding?.txtSize?.text = size2.data
                                        binding?.txtsSize?.text = size2.data
                                    }
                                }
                            }

                        }
                    } else dismiss()
                } catch (ex: java.lang.Exception) {
                    binding?.lytProgress?.visibility = View.GONE
                }
            }

        }
        if(context != null)
        YoYo.with(Techniques.Bounce)
            .duration(700)
            .repeat(10)
            .playOn(binding.btnDownload)
    }

    suspend fun getFileSize(url : String): Resource<String?>? {

        var pData: Resource<String?>? = null
        withContext(Dispatchers.IO) {
            try {
                val myUrl = URL(url)
                val myConnection = myUrl.openConnection()
                myConnection.connectTimeout = 6000
                myConnection.readTimeout = 6000
                val headersize: MutableList<String>? = myConnection.headerFields["content-Length"]
                val lenghtOfFile: Long? = headersize?.get(0)?.toLongOrNull()
                pData = if(lenghtOfFile != null )
                    Resource<String?>(Resource.Status.SUCCESS, FileUtility().getSIzeMB(lenghtOfFile.toLong()), "")
                else Resource<String?>(Resource.Status.SUCCESS, "UNKNOWN", "")
            }
            catch (ex: IllegalArgumentException) {
                pData = Resource<String?>(Resource.Status.ERROR, ex.message, "")
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
    fun select(url1: String, lyt: Int){
        url = url1
        if(lyt == 1) {
            binding?.btnHd?.setBackgroundResource(R.drawable.bg_gold)
            binding?.imageView68?.setImageResource(R.drawable.hd_ic)
            context?.resources?.getColor(
                R.color.white,
                context?.theme
            )?.let {
                binding?.textView228?.setTextColor(
                    it
                )
            }
            context?.resources?.getColor(
                R.color.white,
                context?.theme
            )?.let {
                binding?.txthSize?.setTextColor(
                    it
                )
            }

            binding?.btnSd?.setBackgroundResource(R.drawable.bg_rounded)
            binding?.imageView6?.setImageResource(R.drawable.sd_ic)
            context?.resources?.getColor(
                R.color.black,
                context?.theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }

            context?.resources?.getColor(
                R.color.gray,
                context?.theme
            )?.let {
                binding?.txtsSize?.setTextColor(
                    it
                )
            }
        }
        if(lyt == 2) {
            if(URLUtil.isValidUrl(hdLink)) {
                binding?.btnHd?.setBackgroundResource(R.drawable.bg_rounded)
                binding?.imageView68?.setImageResource(R.drawable.sd_ic)
                context?.resources?.getColor(
                    R.color.black,
                    context?.theme
                )?.let {
                    binding?.textView228?.setTextColor(
                        it
                    )
                }
                context?.resources?.getColor(
                    R.color.gray,
                    context?.theme
                )?.let {
                    binding?.txthSize?.setTextColor(
                        it
                    )
                }
            }

            binding?.btnSd?.setBackgroundResource(R.drawable.bg_gold)
            binding?.imageView6?.setImageResource(R.drawable.hd_ic)
            context?.resources?.getColor(
                R.color.white,
                context?.theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }


            context?.resources?.getColor(
                R.color.white,
                context?.theme
            )?.let {
                binding?.txtsSize?.setTextColor(
                    it
                )
            }
        }
    }
    fun disableHdLayouts(){
        binding?.btnHd?.setBackgroundResource(R.drawable.bg_disabled)
        binding?.imageView68?.setImageResource(R.drawable.hd_ic)
        context?.resources?.let {
            binding?.textView228?.setTextColor(
                it.getColor(
                    R.color.white,
                    context?.theme
                )
            )
        }
        context?.resources?.getColor(
            R.color.white,
            context?.theme
        )?.let {
            binding?.txthSize?.setTextColor(
                it
            )
        }

    }
    private fun disableLayouts(){
        binding?.btnHd?.setBackgroundResource(R.drawable.bg_disabled)
        binding?.imageView68?.setImageResource(R.drawable.hd_ic)
        context?.resources?.getColor(
            R.color.white,
            context?.theme
        )?.let {
            binding?.textView228?.setTextColor(
                it
            )
        }
        context?.resources?.getColor(
            R.color.white,
            context?.theme
        )?.let {
            binding?.txthSize?.setTextColor(
                it
            )
        }

        binding?.btnSd?.setBackgroundResource(R.drawable.bg_disabled)
        binding?.imageView6?.setImageResource(R.drawable.hd_ic)
        context?.resources?.getColor(
            R.color.white,
            context?.theme
        )?.let {
            binding?.textView22?.setTextColor(
                it
            )
        }


        context?.resources?.getColor(
            R.color.white,
            context?.theme
        )?.let {
            binding?.txtsSize?.setTextColor(
                it
            )
        }

    }
    private fun downloadUrl(url : String?){
        //val dm = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        if(url != null && url != "" && url != "null") {
            try {
                val folder = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

                val mBaseFolderPath: String = folder.toString()+ "/"+ "videodownloader"
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

                val exists = DownloadService.downloadsList?.find { s->s.tag == nameUrl}
                if(exists == null) {
                    if(DownloadService.rxFetch?.isClosed == true || DownloadService.rxFetch == null){
                        context?.stopService(Intent(context, DownloadService::class.java))
                        context?.startService(Intent(context, DownloadService::class.java))
                    }
                    Toast.makeText(context, "Downloading Start", Toast.LENGTH_LONG).show()
                    DownloadService.rxFetch?.enqueue(request, {
                        DownloadService.vidCount++
                        DownloadService.videoDownloadStartedLiveData.postValue("1")
                    })
                    {
                        Log.e("downloadUrl", "Error: ${it}", )
                     //   Toast.makeText(context, "Video Cant Be Downloaded!", Toast.LENGTH_LONG).show()
                    }
                }
                else Toast.makeText(context, "Video Already downloaded!", Toast.LENGTH_LONG).show()

            }
            catch(ex: IllegalArgumentException ){
                Toast.makeText(context, "Not Available! "+ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: URISyntaxException){
                Toast.makeText(context, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: ParseException){
                Toast.makeText(context, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
            catch(ex: Exception ){
                Toast.makeText(context, "Not Available! " +ex.message, Toast.LENGTH_LONG).show()
            }
        }
        dismiss()


    }

    var nextIntent :Intent? = null
    var isFinish : Boolean = true
    val startForResult = activity?.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if(nextIntent != null)
                context?.startActivity(nextIntent)
        }
    }
    fun startPremiumAd(intent1: Intent?, finish : Boolean = true){
        isFinish = finish
        nextIntent = intent1
//        val i = Intent(context, PremiumActivity::class.java)
//        i.putExtra("nextActName", "true")
//        startForResult?.launch(i)

//        val dialog = PremiumDialog(_activity)
//        Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(
//            ColorDrawable(
//                Color.TRANSPARENT
//            )
//        )
//        dialog.setCanceledOnTouchOutside(true)
//        dialog.show()
//        dialog.setOnDismissListener {
//            if(nextIntent != null)
//                context?.startActivity(nextIntent)
//        }
//        val window: Window? = dialog.window
//        window?.setLayout(
//            ConstraintLayout.LayoutParams.MATCH_PARENT,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )
    }
    fun getRandomFileName(fileExtension: String): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val randomString = (1..6)
            .map { Random.nextInt(0, 36) }
            .map { if (it < 10) it.toString() else ('a' + it - 10).toString() }
            .joinToString("")

        return "vid_downloader_$timestamp$randomString.$fileExtension"
    }
    fun initDownloader(){

        if(DownloadService.rxFetch == null && context != null) {
            val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(requireContext().applicationContext)
                .setDownloadConcurrentLimit(6)
                .setNamespace("videodwldr")
                .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .build()


            DownloadService.rxFetch = Fetch.getInstance(fetchConfiguration)
        }
    }

}