package com.vid.videodownloader.views.dialogs


import android.app.ActivityOptions
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.URLUtil
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.DownloadPopupFragmentBinding
import com.vid.videodownloader.interfaces.InterAdCallBack
import com.vid.videodownloader.interfaces.OnItemClick
import com.vid.videodownloader.model.RemoteKeys
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.FileUtility
import com.vid.videodownloader.utils.FileUtility.Companion.getDuration
import com.vid.videodownloader.utils.FileUtility.Companion.getDurationString
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jsoup.HttpStatusException
import java.io.File
import java.io.IOException
import java.lang.ClassCastException
import java.lang.IllegalStateException
import java.net.MalformedURLException
import java.net.SocketTimeoutException
import java.net.URISyntaxException
import java.net.URL



class DownloadDialog : DialogFragment()
{
    var name: String? = null
    var sdLink : String?  = null
    var hdLink : String?  = null
    private var binding: DownloadPopupFragmentBinding? = null
    var url : String? = ""
    var watchUrl: String? = ""
    var nameUrl :String? = ""
    var newName = ""
    var hSize = "0 MB"
    var sSize = "0 MB"
    var dataUpdated : MutableLiveData<Int?> = MutableLiveData()
    var watchUrlLoad: String? = ""
    var nameUrlLoad :String? = ""
    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getString("watchUrl")?.let {
            watchUrl = it
        }
        arguments?.getString("nameUrl")?.let {
            nameUrl = it
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DownloadPopupFragmentBinding.inflate(layoutInflater)
        return binding!!.root
    }
    private var onItemClick: OnItemClick<String>? = null
    fun setCallback(activity: OnItemClick<String>){
        onItemClick = activity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )

        Log.v("DownloadProgressDialog", "onCreate")

        binding!!.btnClose.setOnClickListener {
            dismiss()
        }
        binding!!.btnSd.setOnClickListener {
            if(sdLink != null && sdLink != "" && sdLink != "null")
                sdLink?.let { it1 -> select(it1, 2) }
            else if(watchUrl != null && watchUrl != "")
                watchUrl?.let { it1 -> select(it1, 2) }
            else   Toast.makeText(context, "SD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding!!.btnHd.setOnClickListener {
            if(hdLink != null && hdLink != "" && hdLink != "null")
                hdLink?.let { it1 -> select(it1, 1) }
            else   Toast.makeText(context, "HD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding!!.btnDownload.setOnClickListener {
            if(url == null || url == "" || url == "null")
                url = sdLink
            if(url != null && url != "" && url != "null") {
                downloadUrl(url)
            }
            else   Toast.makeText(context, "Video Not Found.", Toast.LENGTH_LONG).show()

        }
        binding!!.btnPlay.setOnClickListener {

            if(sdLink != null && sdLink != "")
                url= sdLink
            else if(watchUrl != null && watchUrl != "")
                url= watchUrl

            if(url != null && url != "") {
                onItemClick?.itemClickResult(url!!,"")
                onItemClick = null
            }
            dismiss()

        }

        val sName = nameUrl?.split("/")
        newName = ""
        if(sName != null && sName.count() > 3) {
            for (i in 0..sName.count()) {
                if(i == sName.count()-1 ||i == sName.count()-2 ||i == sName.count()-3)
                    newName += sName[i]
            }
        }
        name = newName
        Log.v("DownloadProgressDialog", "onCreate"+newName)
        binding!!.txtName.text = newName

        watchUrl?.let { it1 -> select(it1, 2) }
        if(name.isNullOrEmpty() && watchUrl.isNullOrEmpty()){
            binding?.txtName?.text = "Video Not Available"
            binding?.btnDownload?.isEnabled = false
            binding!!.btnPlay.isEnabled = false
            binding!!.btnPlay.visibility = View.INVISIBLE
            disableLayouts()
        }
        if(hdLink.isNullOrEmpty() && !URLUtil.isValidUrl(hdLink)){
            disableHdLayouts()
        }
        if(sdLink != null && URLUtil.isValidUrl(sdLink))
            sdLink?.let { it1 -> select(it1, 2) }


        if (watchUrl == null || watchUrl == "") dismiss()
        longRunningTasks()
        dataUpdated.observe(this, Observer {
            if(it == 1) {
                if(nameUrlLoad != nameUrl && watchUrlLoad != watchUrl) {
                    nameUrl = nameUrlLoad
                    watchUrl = watchUrlLoad
                    val sName2 = nameUrl?.split("/")
                    newName = ""
                    if (sName2 != null && sName2.count() > 3) {
                        for (i in 0..sName2.count()) {
                            if (i == sName2.count() - 1 || i == sName2.count() - 2 || i == sName2.count() - 3)
                                newName += sName2[i]
                        }
                    }
                    name = newName
                    Log.v("DownloadProgressDialog", "onCreate" + newName)
                    binding!!.txtName.text = newName
                    watchUrl?.let { it1 -> select(it1, 2) }
                    if (name.isNullOrEmpty() && watchUrl.isNullOrEmpty()) {
                        binding?.txtName?.text = "Video Not Available"
                        binding?.btnDownload?.isEnabled = false
                        binding!!.btnPlay.isEnabled = false
                        binding!!.btnPlay.visibility = View.INVISIBLE
                        disableLayouts()
                    }
                    if (hdLink.isNullOrEmpty() && !URLUtil.isValidUrl(hdLink)) {
                        disableHdLayouts()
                    }
                    if (sdLink != null && URLUtil.isValidUrl(sdLink))
                        sdLink?.let { it1 -> select(it1, 2) }
                }
            }
        })
    }

    override fun onDetach() {
        super.onDetach()
        onItemClick = null
    }
    override fun onDestroy() {
        super.onDestroy()
        onItemClick = null
    }
    fun longRunningTasks(){
        //val thumb: Long = 1
        //val options = RequestOptions().frame(thumb)
        if(context?.let { StorageSharedPref.isNetworkAvailable(it) } == false){
            Toast.makeText(context, "No Internet!", Toast.LENGTH_LONG).show()
        }
        if(watchUrl != null && watchUrl != "") {
            binding?.btnDownload?.isEnabled = true

        }
        Log.v("DownloadProgressDialog", "onCreate"+sdLink)

        lifecycleScope.launchWhenStarted {
            withContext(Dispatchers.IO){
                try {
                    if (watchUrl != null && watchUrl != "") {
                        var image : Bitmap?= null
                            try {
                                val retriever = MediaMetadataRetriever()
                                retriever.setDataSource(watchUrl, HashMap())
                                image =
                                    retriever.getFrameAtTime(2000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                            }
                            catch (ex: java.lang.IllegalArgumentException){

                            }
                            catch (ex: java.lang.RuntimeException){

                            }
                        val time = watchUrl!!.getDurationString()
                        sSize = FileUtility().getSIzeMB(
                            FileUtility().getSIzeFromDuration(
                                480,
                                watchUrl!!.getDuration()
                            )
                        )
                        val size2 = getFileSize(watchUrl!!)
                        withContext(Dispatchers.Main) {
                            binding?.lytProgress?.visibility = View.GONE
                            binding?.txtsSize?.text = sSize
                            binding?.txtSize?.text = sSize
                            binding?.txtDuration?.text = time
                            if(image != null)
                            context?.let {
                                binding?.imgIcon?.let { it1 ->
                                    Glide.with(it).load(image)
                                        .placeholder(R.drawable.download_ic)
                                        .disallowHardwareConfig()
                                        .error(R.drawable.download_ic)
                                        .into(it1)
                                }
                            }
                            if (size2 != null) {
                                if(size2.status == Resource.Status.SUCCESS){
                                    if(size2.data != "UNKNOWN") {
                                        binding?.txtSize?.text = size2.data
                                        binding?.txtsSize?.text = size2.data
                                    }
                                }
                            }

                        }
                    }
                    else dismiss()
                }
                catch (ex: java.lang.Exception){
                    binding?.lytProgress?.visibility = View.GONE
                }
            }
        }
        if(binding?.btnDownload != null)
        YoYo.with(Techniques.Bounce)
            .duration(700)
            .repeat(10)
            .playOn(binding?.btnDownload)
    }
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
    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    fun select(url1: String, lyt: Int){
        url = url1
        if(lyt == 1) {
            binding?.btnHd?.setBackgroundResource(R.drawable.bg_gold)
            binding?.imageView68?.setImageResource(R.drawable.hd_ic)
            context?.resources?.getColor(
                R.color.white,
                requireContext().theme
            )?.let {
                binding?.textView228?.setTextColor(
                    it
                )
            }
            context?.resources?.getColor(
                R.color.white,
                requireContext().theme
            )?.let {
                binding?.txthSize?.setTextColor(
                    it
                )
            }

            binding?.btnSd?.setBackgroundResource(R.drawable.bg_rounded)
            binding?.imageView6?.setImageResource(R.drawable.sd_ic)
            context?.resources?.getColor(
                R.color.black,
                requireContext().theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }

            context?.resources?.getColor(
                R.color.gray,
                requireContext().theme
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
                    requireContext().theme
                )?.let {
                    binding?.textView228?.setTextColor(
                        it
                    )
                }
                context?.resources?.getColor(
                    R.color.gray,
                    requireContext().theme
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
                requireContext().theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }


            context?.resources?.getColor(
                R.color.white,
                requireContext().theme
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
                    requireContext().theme
                )
            )
        }
        context?.resources?.getColor(
            R.color.white,
            requireContext().theme
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
            requireContext().theme
        )?.let {
            binding?.textView228?.setTextColor(
                it
            )
        }
        context?.resources?.getColor(
            R.color.white,
            requireContext().theme
        )?.let {
            binding?.txthSize?.setTextColor(
                it
            )
        }

                binding?.btnSd?.setBackgroundResource(R.drawable.bg_disabled)
                binding?.imageView6?.setImageResource(R.drawable.hd_ic)
        context?.resources?.getColor(
            R.color.white,
            requireContext().theme
        )?.let {
            binding?.textView22?.setTextColor(
                it
            )
        }


        context?.resources?.getColor(
            R.color.white,
            requireContext().theme
        )?.let {
            binding?.txtsSize?.setTextColor(
                it
            )
        }

    }
    private fun downloadUrl(url : String?){

        Toast.makeText(context, "Downloading Start", Toast.LENGTH_LONG).show()
        val dm = context?.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        if(url != null && url != "" && url != "null") {
            try {
                val folder = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

                val mBaseFolderPath: String =
                    folder.toString()+ "/"+ "videodownloader"
                if (!File(mBaseFolderPath).exists()) {
                    File(mBaseFolderPath).mkdir()
                }
                val mFilePath = "file://$mBaseFolderPath/$name.mp4"
                val downloadUri: Uri = Uri.parse(url)
                val req = DownloadManager.Request(downloadUri)
                req.setTitle(name)
                req.setDescription(nameUrl)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                req.setDestinationUri(Uri.parse(mFilePath))
                else
                req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(mFilePath).path)
                //req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN)
                //req.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                req.allowScanningByMediaScanner()
                req.setAllowedOverMetered(true)
                req.setAllowedOverRoaming(true)
                val id = dm.enqueue(req)
                val pIds = id.toString()+ ","+StorageSharedPref.get("downloadIds")

                StorageSharedPref.save("downloadIds",pIds)
                context?.startService(Intent(context, DownloadService::class.java).putExtra("id",id))
                activity?.let { it1 ->

                }


            }
            catch(ex: IllegalArgumentException ){
                Toast.makeText(context, "Not Available!", Toast.LENGTH_LONG).show()
            }
            catch(ex: URISyntaxException ){
                Toast.makeText(context, "Not Available!", Toast.LENGTH_LONG).show()
            }
            catch(ex: ParseException ){
                Toast.makeText(context, "Not Available!", Toast.LENGTH_LONG).show()
            }
            catch(ex: Exception ){
                Toast.makeText(context, "Not Available!", Toast.LENGTH_LONG).show()
            }
        }
        dismiss()


    }

    override fun show(manager: FragmentManager, tag: String?) {
        try {
            val ft: FragmentTransaction = manager.beginTransaction()
            ft.add(this, tag)
            ft.commitAllowingStateLoss()
        } catch (e: IllegalStateException) {
            Log.d("ABSDIALOGFRAG", "Exception", e)
        }
    }
    companion object {
        fun newInstance(watchUrl : String?, nameUrl: String?) : DownloadDialog {
            val fragment = DownloadDialog()
            fragment.arguments = Bundle().apply {
                putString("watchUrl", watchUrl)
                putString("nameUrl", nameUrl)
            }
            return fragment
        }
    }
}