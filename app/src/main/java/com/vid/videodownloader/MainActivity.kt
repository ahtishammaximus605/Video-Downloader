package com.vid.videodownloader


import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class MainActivity : AppCompatActivity() {
    var webView: WebView? = null
    var defaulturl :String?= "https://m.facebook.com/"
    var url1 :String?= defaulturl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById<View>(R.id.webview) as WebView
        webView!!.settings.javaScriptEnabled = true
        webView!!.settings.builtInZoomControls = true
        webView!!.settings.displayZoomControls = true
        webView!!.settings.useWideViewPort = true
        webView!!.settings.loadWithOverviewMode = true
        webView!!.addJavascriptInterface(this, "FBDownloader")
        webView!!.webViewClient = object : WebViewClient() {

            override fun onLoadResource(view: WebView, url: String) {
                if(url1 != "https://m.facebook.com/")
                {
                    webView!!.loadUrl("javascript:(window.onload = function(e){"
                            +"var login = document.querySelector('#mobile_login_bar');"
                            +"var toplogin = document.querySelector('#login_top_banner');"
                            +"if(toplogin != null){"
                            +"}"
                            +"else if(login != null){"+
                            "FBDownloader.processVideo(null,null,'login');"
                            +"}else{"
                            + "var el = document.querySelectorAll('div[data-sigil]');"
                            +"var count = 0;"
                            + "if(el.length > 0){"
                            + "for(var i=0;i<el.length; i++)"
                            + "{"
                            + "if(el[i] != null){"
                            + "var sigil = el[i].getAttribute('data-sigil');"

                            + "if(sigil == 'inlineVideo'){"
                            +"count = count+1;"
                            + "delete el[i].getAttribute('data-sigil');"
                            + "const btn = document.querySelector('video');"
                            + "if(btn != null){"
                            +"  btn.remove();"
                            +"}"
                            +" var existsbutton = el[i].querySelector('.picon-p-add-news');" +
                            "if(!existsbutton){" +
                            "var buttonEl = document.createElement('div');" +
                            "var buttonTextEl = document.createElement('span');" +
                            "buttonTextEl.className = 'picon-p-add-news';" +
                            "buttonTextEl.style.width = '68px';" +
                            "buttonTextEl.style.height = '68px';" +
                            "buttonTextEl.style.position = 'absolute';" +
                            "buttonTextEl.style.left = '50%';" +
                            "buttonTextEl.style.top = '50%';" +
                            "buttonTextEl.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                            "buttonTextEl.style.backgroundPosition  = '0 0';" +
                            "buttonTextEl.style.backgroundRepeat  = 'no-repeat';" +
                            "buttonTextEl.style.backgroundSize  = 'auto';" +
                            "buttonTextEl.style.margin  = '-34px 0 0 -34px';" +
                            "buttonTextEl.onclick = function (e) {" +
                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
                            "var isPublic = 'Public';"+
                            "if(pOuter != null){"+
                            "                                 var pHeader = pOuter.querySelector('header');" +
                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
                            "                                 isPublic = pAudienceLabel.getAttribute('aria-label');" +
                            "}"+
                            "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                            "};" +
                            "buttonEl.appendChild(buttonTextEl);"
                            +"el[i].appendChild(buttonEl);"
                            +" var buttonElPlanted = el[i].querySelector('.picon-p-add-news');"
                            + "if(count == 1 && buttonElPlanted != null){"

                            +
                            "                                 var jsonData2 = JSON.parse(buttonElPlanted.parentNode.parentNode.getAttribute('data-store')); " +
                            "                                 var pOuter = buttonElPlanted.parentNode.closest('.story_body_container');" +
                            "var isPublic = 'Public';"+
                            "if(pOuter != null){"+
                            "                                 var pHeader = pOuter.querySelector('header');" +
                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
                            "                                 isPublic = pAudienceLabel.getAttribute('aria-label');" +
                            "}"+
                            "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);"
                            +"}"
                            +"}"
                            + "}"


                            + "}"
                            + "}"
                            + "}"
                            + "}"

                            +"})()")
                }
                else
                    webView!!.loadUrl("javascript:(window.onload = function(e){"

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
                            "var buttonEl = document.createElement('div');" +
                            "var buttonTextEl = document.createElement('span');" +
                            "buttonTextEl.className = 'picon-p-add-news';" +
                            "buttonTextEl.style.width = '68px';" +
                            "buttonTextEl.style.height = '68px';" +
                            "buttonTextEl.style.position = 'absolute';" +
                            "buttonTextEl.style.left = '50%';" +
                            "buttonTextEl.style.top = '50%';" +
                            "buttonTextEl.style.backgroundImage  = 'url(https://static.xx.fbcdn.net/rsrc.php/v3/ym/r/3Yq3NMtv823.png)';" +
                            "buttonTextEl.style.backgroundPosition  = '0 0';" +
                            "buttonTextEl.style.backgroundRepeat  = 'no-repeat';" +
                            "buttonTextEl.style.backgroundSize  = 'auto';" +
                            "buttonTextEl.style.margin  = '-34px 0 0 -34px';" +
                            "buttonTextEl.onclick = function (e) {" +
                            "                                 var jsonData2 = JSON.parse(e.target.parentNode.parentNode.getAttribute('data-store')); " +
                            "                                 var pOuter = e.target.parentNode.closest('.story_body_container');" +
                            "                                 var pHeader = pOuter.querySelector('header');" +
                            "                                 var pAudienceLabel = pHeader.querySelector('i.feedAudienceIcon');" +
                            "                                 var isPublic = pAudienceLabel.getAttribute('aria-label');" +
                            "FBDownloader.processVideo(jsonData2['videoURL'],jsonData2['src'],isPublic);" +

                            "};" +
                            "buttonEl.appendChild(buttonTextEl);" +
                            "el[i].appendChild(buttonEl);"

                            + "}"


                            + "}"
                            + "}"
                            + "}"


                            +"})()")
            }
        }
        url1?.let { webView!!.loadUrl(it) }

    }
    @JavascriptInterface
    fun processVideo(vidData: String?, vidID: String, isStatus: String) {
        Log.v("DownloadProgressDialog", "init$isStatus")
        if(isStatus == "login"){
            defaulturl?.let { webView!!.loadUrl(it) }
        }
        else {
            if (isStatus == "Public" || isStatus == "Public group") {
                Log.v("DownloadProgressDialog", "vidUrl=:$vidData")
            }
            else Toast.makeText(this, "Private Video!", Toast.LENGTH_SHORT)
                .show()
        }
    }
}