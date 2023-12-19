package com.vid.videodownloader.views

import android.annotation.SuppressLint
import android.app.DownloadManager
import android.content.Intent
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.vid.videodownloader.databinding.ActivityFbBrowserBinding
import com.vid.videodownloader.model.RemoteKeys
import com.vid.videodownloader.model.Resource
import com.vid.videodownloader.viewmodel.FbBrowserViewModel
import com.vid.videodownloader.views.fragments.NavigationFragment
import kotlinx.coroutines.*
import java.util.*
import android.webkit.WebView

import android.webkit.WebViewClient
import android.content.DialogInterface
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.ParseException
import android.net.Uri
import android.os.*
import android.webkit.CookieManager
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.text.htmlEncode
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import com.vid.videodownloader.R
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.FileUtility
import com.vid.videodownloader.utils.FileUtility.Companion.getDurationString
import com.vid.videodownloader.utils.StorageSharedPref
import org.jsoup.HttpStatusException
import java.io.File
import java.io.IOException
import java.net.*


class FbBrowserActivity : BaseActivity<ActivityFbBrowserBinding>({ ActivityFbBrowserBinding.inflate(it) }) {
    var webView: WebView? = null
    var lytOuter: FrameLayout? = null
    private val mViewModel : FbBrowserViewModel by viewModels()
    var v =0
    var dialog : ConstraintLayout? = null
    var defaulturl :String?= "https://m.facebook.com/"
    var url1 :String?= defaulturl
    var intentUrl :String?= null
    var wasAutoCopyUrl : String? = ""
    var newintentUrl :String?= null
    var image: String? = null

    var name: String? = null
    var sdLink : String?  = null
    var hdLink : String?  = null
    var url : String? = ""
    var watchUrl: String? = ""
    var nameUrl :String? = ""
    var newName = ""
    var hSize = "0 MB"
    var sSize = "0 MB"
    var dataUpdated : MutableLiveData<Int?> = MutableLiveData()
    var watchUrlLoad: String? = ""
    var nameUrlLoad :String? = ""

    override fun onResume() {
        super.onResume()
        binding.progressBar6.visibility = View.GONE
        initDownloader()
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        Log.v("FbBrowserActivity", "" )
        super.onCreate(savedInstanceState)
        mViewModel.init()
        dialog = binding.dialog
        if(savedInstanceState == null)
        supportFragmentManager.beginTransaction()
            .add(binding.navigationContent.id, NavigationFragment.newInstance(2))
            .setReorderingAllowed(true)
            .commit()
        if(intent.extras?.getString("url") != null && intent.extras?.getString("url") != "") {
            url1 = intent.extras?.getString("url")
            intentUrl  = intent.extras?.getString("url")
        }
        initBrowser()
        if(DownloadService.rxFetch?.isClosed == true || DownloadService.rxFetch == null){
            stopService(Intent(this, DownloadService::class.java))
            startService(Intent(this, DownloadService::class.java))
        }
        dataUpdated.observe(this, androidx.lifecycle.Observer {
            if(it == 1) {

                binding.progressBar6.visibility = View.GONE
                dialog?.visibility = View.VISIBLE
                if (nameUrl?.endsWith("/") == true) {
                    nameUrl = nameUrl?.substring(0, nameUrl!!.length - 1)
                }
                val sName = nameUrl?.split("/")

                newName = sName?.get(sName.size-1)?:""
                name = newName
                Log.v("DownloadProgressDialog", "onCreate"+newName)
                binding.txtName.text = newName

                watchUrl?.let { it1 -> select(it1, 2) }
                if(name.isNullOrEmpty() && watchUrl.isNullOrEmpty()){
                    binding.txtName.text = "Video Not Available"
                    binding.btnDownload.isEnabled = false
                    binding.btnPlay.isEnabled = false
                    binding.btnPlay.visibility = View.INVISIBLE
                    disableLayouts()
                }
                if(hdLink.isNullOrEmpty() && !URLUtil.isValidUrl(hdLink)){
                    disableHdLayouts()
                }
                if(sdLink != null && URLUtil.isValidUrl(sdLink))
                    sdLink?.let { it1 -> select(it1, 2) }

                if (watchUrl == null || watchUrl == "") dialog?.visibility = View.GONE
                longRunningTasks()
                if(!StorageSharedPref.isNetworkAvailable(this)){
                    Toast.makeText(this, "No Internet!", Toast.LENGTH_LONG).show()
                }
//                if(watchUrl != null && watchUrl != "") {
//                    binding.btnDownload.isEnabled = true
//
//                }
                //binding.lytProgress.visibility = View.GONE
            }
        })
        binding.btnClose.setOnClickListener {
            binding.progressBar6.visibility = View.GONE
            dialog?.visibility = View.GONE
        }
        binding.btnSd.setOnClickListener {
            if(sdLink != null && sdLink != "" && sdLink != "null")
                sdLink?.let { it1 -> select(it1, 2) }
            else if(watchUrl != null && watchUrl != "")
                watchUrl?.let { it1 -> select(it1, 2) }
            else   Toast.makeText(this, "SD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding.btnHd.setOnClickListener {
            if(hdLink != null && hdLink != "" && hdLink != "null")
                hdLink?.let { it1 -> select(it1, 1) }
            else   Toast.makeText(this, "HD Quality not available.", Toast.LENGTH_LONG).show()

        }
        binding.btnDownload.setOnClickListener {
            if(url == null || url == "" || url == "null")
                url = sdLink
            if(url != null && url != "" && url != "null") {
                downloadUrl(url)
            }
            else   Toast.makeText(this, "Video Not Found.", Toast.LENGTH_LONG).show()

        }
        binding.btnPlay.setOnClickListener {

            if(sdLink != null && sdLink != "")
                url= sdLink
            else if(watchUrl != null && watchUrl != "")
                url= watchUrl

            if(watchUrl != null && watchUrl != "") {
                itemClickResult(watchUrl!!,"")
            }
            dialog?.visibility = View.GONE

        }
    }

    fun initBrowser(){
        lytInterProgress = binding.lytInterProgress
        wasAutoCopyUrl = url1
        if(url1?.startsWith("https://m.facebook.com/") == false
            && url1?.startsWith("https://www.facebook.com/") == false
            && url1?.startsWith("https://fb.watch/") == false)
            url1 = defaulturl

        lytOuter = binding.webview
        webView =  WebView(applicationContext)
        lytOuter!!.addView(webView)

        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.builtInZoomControls = true
        webView!!.settings.displayZoomControls = true
        webView!!.settings.useWideViewPort = true
        webView!!.settings.loadWithOverviewMode = true
        webView!!.settings.textZoom = 100
        if(intent.extras?.getString("logout") == "1"){
            webView!!.clearHistory()
            webView!!.clearFormData()
            CookieManager.getInstance().removeAllCookies(null)
        }

        webView!!.addJavascriptInterface(this, "FBDownloader")
        webView!!.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                return if (request?.url.toString().contains("http:")) {
                    val usss = request?.url.toString().replace("http:", "https:")
                    view!!.loadUrl(usss)
                    true
                }else false
            }
            override fun onPageFinished(view: WebView?, url: String?) {

                if( url1 != "https://m.facebook.com/") {
                    webView?.loadUrl("javascript:(window.onload = function(e){"
                            + "var el = document.querySelectorAll('div[data-sigil]');"
                            + "if(el.length > 0){"
                            + "for(var i=0;i<el.length; i++)"
                            + "{"
                            + "var sigil = el[i].getAttribute('data-sigil');"

                            + "if(sigil == 'inlineVideo'){"
                            + "delete el[i].getAttribute('data-sigil');"
                            + "const btn = document.querySelector('video');"
                            + "if(btn != null){"
                            +"  btn.remove();"
                            +"}"
                            +" var existsbutton = el[i].querySelector('.picon-p-add-news');" +
                            "if(!existsbutton){" +
                            "var btnNewPlayDownOuter = document.createElement('div');" +
                            "var btnNewPlayDown = document.createElement('span');" +
                            "btnNewPlayDown.className = 'picon-p-add-news';" +
                            "btnNewPlayDown.id = 'picon-p-add-news-'+i;" +
                            "btnNewPlayDown.style.width = '68px';" +
                            "btnNewPlayDown.style.height = '68px';" +
                            "btnNewPlayDown.style.position = 'absolute';" +
                            "btnNewPlayDown.style.left = '50%';" +
                            "btnNewPlayDown.style.top = '50%';" +
                            "btnNewPlayDown.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                            "btnNewPlayDown.style.backgroundPosition  = '0 0';" +
                            "btnNewPlayDown.style.backgroundRepeat  = 'no-repeat';" +
                            "btnNewPlayDown.style.backgroundSize  = 'auto';" +
                            "btnNewPlayDown.style.margin  = '-34px 0 0 -34px';" +
                            "btnNewPlayDown.onclick = function (e) {" +
                            "   var jsonData2 = '';"+
                            "   var isPublic = '';"+
                            "console.log('hello');"+
                            "const btn = document.querySelector('video');"+
                            "if(btn != null){"+
                            "  btn.remove();"+
                            "}"+
                            "console.log('btn');"+
                            "   var jsonElem = e.target.parentNode.closest('div[data-store]');"+
                            "   if(jsonElem != null){"+
                            "        jsonData2 = JSON.parse(jsonElem.getAttribute('data-store')); " +
                            "        console.log(jsonData2);"+

                            "   var outerParent = jsonElem.parentNode.closest('div[data-store-id]');"+
                            "   if(outerParent == null) { outerParent = jsonElem.parentNode.closest('.story_body_container');}"+
                            "   if(outerParent != null){"+
                            "console.log(outerParent.outerHTML);"+
                            "console.log(outerParent.textContent);"+

                            "     var head = outerParent.querySelector('.feedAudienceIcon');" +
                            "console.log(head);"+
                            "     if(head != null){"+
                            "         isPublic = head.getAttribute('aria-label');" +
                            "      }"+

                            "    }"+
                            "    }"+
//                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
//                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
//                            "                                 var pHeader = pOuter.querySelector('header');" +
//                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
//                            "                                 var isPublic = pAudienceLabel.getAttribute('aria-label');" +
//
                            "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                            "};" +
                            "btnNewPlayDownOuter.appendChild(btnNewPlayDown);" +
                            "el[i].appendChild(btnNewPlayDownOuter);"

                            + "}"
                            + "}"
                            + "}"
                            + "}"

                            +"})()")
                }
                else {
                    webView?.loadUrl(
                        "javascript:(window.onload = function(e){"

                                + "var el = document.querySelectorAll('div[data-sigil]');"
                                + "if(el.length > 0){"
                                + "for(var i=0;i<el.length; i++)"
                                + "{"
                                + "var sigil = el[i].getAttribute('data-sigil');"

                                + "if(sigil == 'inlineVideo'){"
                                + "delete el[i].getAttribute('data-sigil');"
                                + "const btn = document.querySelector('video');"
                                + "if(btn != null){"
                                + "  btn.remove();"
                                + "}"
                                + " var existsbutton = el[i].querySelector('.picon-p-add-news');" +
                                "if(!existsbutton){" +
                                "var btnNewPlayDownOuter = document.createElement('div');" +
                                "var btnNewPlayDown = document.createElement('span');" +
                                "btnNewPlayDown.className = 'picon-p-add-news';" +
                                "btnNewPlayDown.style.width = '68px';" +
                                "btnNewPlayDown.style.height = '68px';" +
                                "btnNewPlayDown.style.position = 'absolute';" +
                                "btnNewPlayDown.style.left = '50%';" +
                                "btnNewPlayDown.style.top = '50%';" +
                                "btnNewPlayDown.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                                "btnNewPlayDown.style.backgroundPosition  = '0 0';" +
                                "btnNewPlayDown.style.backgroundRepeat  = 'no-repeat';" +
                                "btnNewPlayDown.style.backgroundSize  = 'auto';" +
                                "btnNewPlayDown.style.margin  = '-34px 0 0 -34px';" +
                                "btnNewPlayDown.onclick = function (e) {" +
                                "   var jsonData2 = '';"+
                                "   var isPublic = '';"+
                                "console.log('hello');"+
                                "const btn = document.querySelector('video');"+
                                "if(btn != null){"+
                                "  btn.remove();"+
                                "}"+
                                "console.log('btn');"+
                                "   var jsonElem = e.target.parentNode.closest('div[data-store]');"+
                                "   if(jsonElem != null){"+
                                "        jsonData2 = JSON.parse(jsonElem.getAttribute('data-store')); " +
                                "        console.log(jsonData2);"+

                                "   var outerParent = jsonElem.parentNode.closest('div[data-store-id]');"+
                                "   if(outerParent == null) { outerParent = jsonElem.parentNode.closest('.story_body_container');}"+
                                "   if(outerParent != null){"+
                                "console.log(outerParent.outerHTML);"+
                                "console.log(outerParent.textContent);"+

                                "     var head = outerParent.querySelector('.feedAudienceIcon');" +
                                "console.log(head);"+
                                "     if(head != null){"+
                                "         isPublic = head.getAttribute('aria-label');" +
                                "      }"+

                                "    }"+
                                "    }"+
//                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
//                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
//                            "                                 var pHeader = pOuter.querySelector('header');" +
//                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
//                            "                                 var isPublic = pAudienceLabel.getAttribute('aria-label');" +
//
                                "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                                "};" +
                                "btnNewPlayDownOuter.appendChild(btnNewPlayDown);" +
                                "el[i].appendChild(btnNewPlayDownOuter);"

                                + "}"


                                + "}"
                                + "}"
                                + "}"


                                + "})()"
                    )
                }
            }

            override fun onLoadResource(view: WebView, url: String) {
                if( url1 != "https://m.facebook.com/") {
                    webView?.loadUrl("javascript:(window.onload = function(e){"
                            + "var el = document.querySelectorAll('div[data-sigil]');"
                            + "if(el.length > 0){"
                            + "for(var i=0;i<el.length; i++)"
                            + "{"
                            + "var sigil = el[i].getAttribute('data-sigil');"

                            + "if(sigil == 'inlineVideo'){"
                            + "delete el[i].getAttribute('data-sigil');"
                            + "const btn = document.querySelector('video');"
                            + "if(btn != null){"
                            +"  btn.remove();"
                            +"}"
                            +" var existsbutton = el[i].querySelector('.picon-p-add-news');" +
                            "if(!existsbutton){" +
                            "var btnNewPlayDownOuter = document.createElement('div');" +
                            "var btnNewPlayDown = document.createElement('span');" +
                            "btnNewPlayDown.className = 'picon-p-add-news';" +
                            "btnNewPlayDown.id = 'picon-p-add-news-'+i;" +
                            "btnNewPlayDown.style.width = '68px';" +
                            "btnNewPlayDown.style.height = '68px';" +
                            "btnNewPlayDown.style.position = 'absolute';" +
                            "btnNewPlayDown.style.left = '50%';" +
                            "btnNewPlayDown.style.top = '50%';" +
                            "btnNewPlayDown.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                            "btnNewPlayDown.style.backgroundPosition  = '0 0';" +
                            "btnNewPlayDown.style.backgroundRepeat  = 'no-repeat';" +
                            "btnNewPlayDown.style.backgroundSize  = 'auto';" +
                            "btnNewPlayDown.style.margin  = '-34px 0 0 -34px';" +
                            "btnNewPlayDown.onclick = function (e) {" +
                            "   var jsonData2 = '';"+
                            "   var isPublic = '';"+
                            "console.log('hello');"+
                            "const btn = document.querySelector('video');"+
                            "if(btn != null){"+
                            "  btn.remove();"+
                            "}"+
                            "console.log('btn');"+
                            "   var jsonElem = e.target.parentNode.closest('div[data-store]');"+
                            "   if(jsonElem != null){"+
                            "        jsonData2 = JSON.parse(jsonElem.getAttribute('data-store')); " +
                            "        console.log(jsonData2);"+

                            "   var outerParent = jsonElem.parentNode.closest('div[data-store-id]');"+
                            "   if(outerParent == null) { outerParent = jsonElem.parentNode.closest('.story_body_container');}"+
                            "   if(outerParent != null){"+
                            "console.log(outerParent.outerHTML);"+
                            "console.log(outerParent.textContent);"+

                            "     var head = outerParent.querySelector('.feedAudienceIcon');" +
                            "console.log(head);"+
                            "     if(head != null){"+
                            "         isPublic = head.getAttribute('aria-label');" +
                            "      }"+

                            "    }"+
                            "    }"+
//                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
//                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
//                            "                                 var pHeader = pOuter.querySelector('header');" +
//                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
//                            "                                 var isPublic = pAudienceLabel.getAttribute('aria-label');" +
//
                            "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                            "};" +
                            "btnNewPlayDownOuter.appendChild(btnNewPlayDown);" +
                            "el[i].appendChild(btnNewPlayDownOuter);"

                            + "}"
                            + "}"
                            + "}"
                            + "}"

                            +"})()")
                }
                else {
                    webView?.loadUrl(
                        "javascript:(window.onload = function(e){"

                                + "var el = document.querySelectorAll('div[data-sigil]');"
                                + "if(el.length > 0){"
                                + "for(var i=0;i<el.length; i++)"
                                + "{"
                                + "var sigil = el[i].getAttribute('data-sigil');"

                                + "if(sigil == 'inlineVideo'){"
                                + "delete el[i].getAttribute('data-sigil');"
                                + "const btn = document.querySelector('video');"
                                + "if(btn != null){"
                                + "  btn.remove();"
                                + "}"
                                + " var existsbutton = el[i].querySelector('.picon-p-add-news');" +
                                "if(!existsbutton){" +
                                "var btnNewPlayDownOuter = document.createElement('div');" +
                                "var btnNewPlayDown = document.createElement('span');" +
                                "btnNewPlayDown.className = 'picon-p-add-news';" +
                                "btnNewPlayDown.style.width = '68px';" +
                                "btnNewPlayDown.style.height = '68px';" +
                                "btnNewPlayDown.style.position = 'absolute';" +
                                "btnNewPlayDown.style.left = '50%';" +
                                "btnNewPlayDown.style.top = '50%';" +
                                "btnNewPlayDown.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                                "btnNewPlayDown.style.backgroundPosition  = '0 0';" +
                                "btnNewPlayDown.style.backgroundRepeat  = 'no-repeat';" +
                                "btnNewPlayDown.style.backgroundSize  = 'auto';" +
                                "btnNewPlayDown.style.margin  = '-34px 0 0 -34px';" +
                                "btnNewPlayDown.onclick = function (e) {" +
                                "   var jsonData2 = '';"+
                                "   var isPublic = '';"+
                                "console.log('hello');"+
                                "const btn = document.querySelector('video');"+
                                "if(btn != null){"+
                                "  btn.remove();"+
                                "}"+
                                "console.log('btn');"+
                                "   var jsonElem = e.target.parentNode.closest('div[data-store]');"+
                                "   if(jsonElem != null){"+
                                "        jsonData2 = JSON.parse(jsonElem.getAttribute('data-store')); " +
                                "        console.log(jsonData2);"+

                                "   var outerParent = jsonElem.parentNode.closest('div[data-store-id]');"+
                                "   if(outerParent == null) { outerParent = jsonElem.parentNode.closest('.story_body_container');}"+
                                "   if(outerParent != null){"+
                                "console.log(outerParent.outerHTML);"+
                                "console.log(outerParent.textContent);"+

                                "     var head = outerParent.querySelector('.feedAudienceIcon');" +
                                "console.log(head);"+
                                "     if(head != null){"+
                                "         isPublic = head.getAttribute('aria-label');" +
                                "      }"+

                                "    }"+
                                "    }"+
//                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
//                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
//                            "                                 var pHeader = pOuter.querySelector('header');" +
//                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
//                            "                                 var isPublic = pAudienceLabel.getAttribute('aria-label');" +
//
                                "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                                "};" +
                                "btnNewPlayDownOuter.appendChild(btnNewPlayDown);" +
                                "el[i].appendChild(btnNewPlayDownOuter);"

                                + "}"


                                + "}"
                                + "}"
                                + "}"


                                + "})()"
                    )
                }
            }
        }

        url1?.let { webView!!.loadUrl(it) }
//        if(url1 != null && url1 != defaulturl){
//            binding.progressBar6.visibility = View.VISIBLE
//            webView!!.visibility = View.INVISIBLE
//        }
    }

    var isLoginScreen = false
    var isPageUrl = false

    @JavascriptInterface
    fun processVideo(vidData: String?, vidID: String?, isPublic: String?) {
        Log.v("DownloadProgressDialog", "init$isPublic")

        if(isPublic == "novideo")
        {
            Toast.makeText(this@FbBrowserActivity, "Video Not Available! Try Another Video.", Toast.LENGTH_SHORT)
                .show()
            //startActivity(Intent(this, DownloadLinkActivity::class.java))
            //finish()
        }
        else if(isPublic == "alreadylogged"){
            runOnUiThread {
                binding.progressBar6.visibility = View.GONE
                webView?.visibility = View.VISIBLE
            }
        }
        else if(isPublic == "logged"){
            isPageUrl = true
            runOnUiThread {
                webView?.loadUrl("about:blank")
                url1?.let { webView!!.loadUrl(it) }
                binding.progressBar6.visibility = View.GONE
                webView?.visibility = View.VISIBLE
            }
        }
        else if(isPublic == "login"){
            if(!isLoginScreen) {
                isLoginScreen = true
                runOnUiThread {
                    webView?.loadUrl("about:blank")
                    webView?.loadUrl("https://m.facebook.com/login.php")
                    webView?.visibility = View.VISIBLE
                    binding.progressBar6.visibility = View.GONE


                     Toast.makeText(this, "You need to login to download group content!", Toast.LENGTH_LONG).show()
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    val dg = builder.create()
                    builder.setMessage("You need to login to download group video content!")
                        .setNegativeButton("Ok",
                            DialogInterface.OnClickListener { dialog, which -> dg.dismiss() })

                    builder.show()

                }
                println("javscript done..")
            }
        }
        else {
            isLoginScreen = false
            v++
            Log.v("DownloadProgressDialog", "count:$v=$vidData")
            if (isPublic == "Public" || isPublic == "Public group" || isPublic =="") {
                Log.v("DownloadProgressDialog", "public=:$isPublic")
                job?.cancel()
                job = lifecycleScope.launch(Dispatchers.IO) {
                    if(isActive) {
                        binding.progressBar6.visibility = View.GONE
                        watchUrlLoad = vidID
                        nameUrlLoad = vidData

                        watchUrl = vidID
                        nameUrl = vidData
                        dataUpdated.postValue(1)
                    }
                }

            }
            else Toast.makeText(this@FbBrowserActivity, "Private Video!", Toast.LENGTH_SHORT)
                .show()
        }
    }


    override fun onBackPressed() {
        if (webView?.canGoBack() == true && !isLoginScreen) {
            webView?.goBack()
        } else {
            startActivity(Intent(this, DashboardActivity::class.java))
          //  startAdActivity(Intent(this, DashboardActivity::class.java), RemoteKeys.tab_back_inters, this)
        }
    }
    var retriever : MediaMetadataRetriever? = null
    fun itemClickResult(w: String, name: String) {

                        binding.progressBar6.visibility = View.GONE
                        startActivity(
                            Intent(
                                this@FbBrowserActivity,
                                PlayerActivity::class.java
                            ).putExtra("url", w))
    }
    var job : Job? = null
    fun longRunningTasks(){
        binding.lytProgress.visibility = View.VISIBLE
        if(!StorageSharedPref.isNetworkAvailable(this)){
            Toast.makeText(this, "No Internet!", Toast.LENGTH_LONG).show()
        }
        if(watchUrl != null && watchUrl != "") {
            binding.btnDownload.isEnabled = true

        }

        Log.v("DownloadProgressDialog", "onCreate"+sdLink)
        job?.cancel()
        var s : Long = 0

        retriever?.release()
        job = lifecycleScope.launch(Dispatchers.IO) {
            if(isActive) {
                        var isSuccess = false
                        var time: String = ""
                        var size2: Resource<String?>? = null
                        try {
                            if (watchUrl != null && watchUrl != "") {
                                try {
                                    retriever = MediaMetadataRetriever()
                                    retriever?.setDataSource(watchUrl, HashMap())
                                    retriever?.getFrameAtTime(
                                            200000
                                        )
                                    val d = retriever?.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                                    retriever?.release()
                                    s = d?.toLongOrNull() ?: 0
                                } catch (ex: java.lang.IllegalArgumentException) {
                                    retriever?.release()
                                } catch (ex: java.lang.RuntimeException) {
                                    retriever?.release()
                                }

                                time = s.getDurationString()
                                sSize = FileUtility().getSIzeMB(
                                    FileUtility().getSIzeFromDuration(
                                        480,
                                        s
                                    )
                                )
                                size2 = getFileSize(watchUrl!!)

                            } else isSuccess = false
                        } catch (ex: java.lang.Exception) {
                            isSuccess = false
                        }
                        withContext(Dispatchers.Main) {
                            if(!isSuccess) {
                                dialog?.visibility = View.GONE
                                binding.lytProgress.visibility = View.GONE
                            }else {
                                binding?.lytProgress?.visibility = View.GONE
                                binding?.txtsSize?.text = sSize
                                binding?.txtSize?.text = sSize
                                binding?.txtDuration?.text = time
                                if (image != null)

                                    binding?.imgIcon?.let { it1 ->
                                        Glide.with(this@FbBrowserActivity).load(image)
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
                        }


            }
        }

            YoYo.with(Techniques.Bounce)
                .duration(700)
                .repeat(10)
                .playOn(binding.btnDownload)

//        Handler(Looper.getMainLooper()).postDelayed({
//                retriever?.release()
//                retriever?.close()
//                retriever = null
//        }, 6000)
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
            this.resources?.getColor(
                R.color.white,
                this.theme
            )?.let {
                binding?.textView228?.setTextColor(
                    it
                )
            }
            this.resources?.getColor(
                R.color.white,
                this.theme
            )?.let {
                binding?.txthSize?.setTextColor(
                    it
                )
            }

            binding?.btnSd?.setBackgroundResource(R.drawable.bg_rounded)
            binding?.imageView6?.setImageResource(R.drawable.sd_ic)
            this.resources?.getColor(
                R.color.black,
                this.theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }

            this.resources?.getColor(
                R.color.gray,
                this.theme
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
                this.resources?.getColor(
                    R.color.black,
                    this.theme
                )?.let {
                    binding?.textView228?.setTextColor(
                        it
                    )
                }
                this.resources?.getColor(
                    R.color.gray,
                    this.theme
                )?.let {
                    binding?.txthSize?.setTextColor(
                        it
                    )
                }
            }

            binding?.btnSd?.setBackgroundResource(R.drawable.bg_gold)
            binding?.imageView6?.setImageResource(R.drawable.hd_ic)
            this.resources?.getColor(
                R.color.white,
                this.theme
            )?.let {
                binding?.textView22?.setTextColor(
                    it
                )
            }


            this.resources?.getColor(
                R.color.white,
                this.theme
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
        this.resources?.let {
            binding?.textView228?.setTextColor(
                it.getColor(
                    R.color.white,
                    this.theme
                )
            )
        }
        this.resources?.getColor(
            R.color.white,
            this.theme
        )?.let {
            binding?.txthSize?.setTextColor(
                it
            )
        }


    }
    private fun disableLayouts(){
        binding?.btnHd?.setBackgroundResource(R.drawable.bg_disabled)
        binding?.imageView68?.setImageResource(R.drawable.hd_ic)
        this.resources?.getColor(
            R.color.white,
            this.theme
        )?.let {
            binding?.textView228?.setTextColor(
                it
            )
        }
        this.resources?.getColor(
            R.color.white,
            this.theme
        )?.let {
            binding?.txthSize?.setTextColor(
                it
            )
        }

        binding?.btnSd?.setBackgroundResource(R.drawable.bg_disabled)
        binding?.imageView6?.setImageResource(R.drawable.hd_ic)
        this.resources?.getColor(
            R.color.white,
            this.theme
        )?.let {
            binding?.textView22?.setTextColor(
                it
            )
        }


        this.resources?.getColor(
            R.color.white,
            this.theme
        )?.let {
            binding?.txtsSize?.setTextColor(
                it
            )
        }

    }

    private fun downloadUrl(url : String?){
        //val dm = this.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        if(url != null && url != "" && url != "null") {
            try {
                val folder = this.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)

                val mBaseFolderPath: String =
                    folder.toString()+ "/"+ "videodownloader"
                if (!File(mBaseFolderPath).exists()) {
                    File(mBaseFolderPath).mkdir()
                }
                val filePath: String = folder.toString() + "/videodownloader/" + Uri.parse(url).lastPathSegment

                val request = Request(url, filePath)
                request.priority = Priority.HIGH
                request.networkType = NetworkType.ALL
                request.tag = nameUrl
                request.groupId = groupId

               // val it  = StorageVideos.getVids()

                val exists = DownloadService.downloadsList?.find { s->s.tag == nameUrl}
                if(exists == null) {
                    if(DownloadService.rxFetch?.isClosed == true || DownloadService.rxFetch == null){
                        stopService(Intent(this, DownloadService::class.java))
                        startService(Intent(this, DownloadService::class.java))
                    }
                        Toast.makeText(this, "Downloading Start", Toast.LENGTH_LONG).show()
                        DownloadService.rxFetch?.enqueue(request, {
                            DownloadService.vidCount++
                            DownloadService.videoDownloadStartedLiveData.postValue("1")
                        })
                        {
                            Toast.makeText(this, "Video Cant Be Downloaded!", Toast.LENGTH_LONG).show()
                        }
                    }
                    else Toast.makeText(this, "Video Already downloaded!", Toast.LENGTH_LONG).show()



//                val mFilePath = "file://$mBaseFolderPath/$name.mp4"
//                val downloadUri: Uri = Uri.parse(url)
//                val req = DownloadManager.Request(downloadUri)
//                req.setTitle(name)
//                req.setDescription(nameUrl)
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
//                    req.setDestinationUri(Uri.parse(mFilePath))
//                else
//                    req.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, Uri.parse(mFilePath).path)
//                req.allowScanningByMediaScanner()
//                req.setAllowedOverMetered(true)
//                req.setAllowedOverRoaming(true)
//
//
//                val id = dm.enqueue(req)
//                val pIds = id.toString()+ ","+ StorageSharedPref.get("downloadIds")
//
//                StorageSharedPref.save("downloadIds",pIds)


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
        dialog?.visibility = View.GONE


    }

    fun initDownloader(){

        if(DownloadService.rxFetch == null) {
            val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(applicationContext)
                .setDownloadConcurrentLimit(6)
                .setNamespace("videodwldr")
                .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .build()


            DownloadService.rxFetch = Fetch.getInstance(fetchConfiguration)
        }
    }
}