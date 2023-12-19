package com.vid.videodownloader.views


import android.content.*
import android.graphics.Color
import android.graphics.drawable.ColorDrawable

import android.os.*
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager

import android.webkit.URLUtil
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo

import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.ActivityDownloadLinkBinding
import com.vid.videodownloader.interfaces.OnDownloadClick

import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.DashboardActivity.Companion.dashboardClick
import com.vid.videodownloader.views.SplashActivity.Companion.adsplashisready
import com.vid.videodownloader.views.SplashActivity.Companion.config
import com.vid.videodownloader.views.dialogs.ExitDialog
import com.vid.videodownloader.views.dialogs.LoadingDialog
import com.vid.videodownloader.views.fragments.NavigationFragment
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Objects
import java.util.concurrent.TimeUnit


class DownloadLinkActivity : BaseActivity<ActivityDownloadLinkBinding>({ ActivityDownloadLinkBinding.inflate(it) }),
    OnDownloadClick {
    private var loadingDialog: LoadingDialog? = null
    private var adisready = "notshowed"
    private var splashadisready = "notshowed"
    private var isActivityRunning: Boolean = false
    private var baseUrlAnything = ""
    private var baseUrlLink = ""

    companion object {
        var downloadListener: (() -> Unit)? = null
        var workAfterAd: (() -> Unit)? = null
        var videoAlready: (() -> Unit)? = null
        var errorDownloading: ((errorTitle:String,error:String) -> Unit)? = null
        private var clicked: Boolean = false
         var backPressed: Boolean = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lytInterProgress = binding.lytInterProgress
        if (config.base_url_anything.value.isNotEmpty()) {
            baseUrlAnything = config.base_url_anything.value
        } else {
            baseUrlAnything = "https://downloader.thinkshot.site/"
        }

        if (config.base_url_link.value.isNotEmpty()) {
            baseUrlLink = config.base_url_link.value
        } else {
            baseUrlLink = "https://downloader.thinkshot.site/"
        }

//        baseUrlAnything = "https://mocki.io/v1/"
//        baseUrlLink = "https://mocki.io/v1/"

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(80.toLong(), TimeUnit.SECONDS)
            .readTimeout(80.toLong(), TimeUnit.SECONDS)
            .build()

        retrofit = Retrofit.Builder()
            .baseUrl(baseUrlAnything)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofitLink = Retrofit.Builder()
            .baseUrl(baseUrlLink)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        type = 1
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .add(binding.navigationContent.id, NavigationFragment.newInstance(1))
                .setReorderingAllowed(true)
                .commit()
        binding.button.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextTextPersonName.windowToken, 0)
            val url = binding.editTextTextPersonName.text.toString()
            if (URLUtil.isValidUrl(url)) {
                binding.button.visibility = View.INVISIBLE
                binding.loadingVid.visibility = View.VISIBLE
                binding.downloaded.visibility=View.GONE
                clicked=false
                workAfterAd={
                    processVideo(url) {
                        binding.editTextTextPersonName.setText("")
                        binding.button.visibility = View.VISIBLE
                        binding.loadingVid.visibility = View.GONE
                    }
                }
                val intent=Intent(this,DownloadingStateActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)

            } else {Toast.makeText(this, "Not valid video URL.", Toast.LENGTH_SHORT).show()
                binding.editTextTextPersonName.setText("")
            }

        }

        loadingDialog = LoadingDialog(this)
        val showAd = intent?.getStringExtra("showAd")
        if (showAd == "1") {

        }


        binding.editTextTextPersonName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().length > 0) {
                    binding.button.setBackgroundResource(R.drawable.background_blue)
                    binding.hint.visibility=View.GONE
                    binding.clearText.visibility = View.VISIBLE
                    binding.button.isClickable = true
                    binding.button.isEnabled = true
                    binding.paste.setBackgroundResource(R.drawable.background_dark)
                    YoYo.with(Techniques.Shake)
                        .duration(800)
                        .repeat(1)
                        .playOn(binding.button)

                } else {
                    binding.hint.visibility=View.VISIBLE
                    binding.button.setBackgroundResource(R.drawable.background_dark)
                    binding.paste.setBackgroundResource(R.drawable.background_blue)
                    binding.clearText.visibility = View.GONE
                    binding.button.isClickable = false
                    binding.button.isEnabled = false
                }
            }
        })
//        DownloadService.updated.observe(this, Observer { download ->
////            if (download != null) {
////                updateUI(download)
////            }
//        })

        binding.clearText.setOnClickListener {
            binding.editTextTextPersonName.setText("")
        }

        binding.paste.setOnClickListener {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editTextTextPersonName.windowToken, 0)
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (clipboard.hasPrimaryClip() && clipboard.primaryClipDescription!!.hasMimeType(
                    ClipDescription.MIMETYPE_TEXT_PLAIN
                )
            ) {
                // The clipboard contains text data
                val item = clipboard.primaryClip!!.getItemAt(0)
                val text = item.text.toString().trim()
                binding.editTextTextPersonName.setText(text)


                binding.editTextTextPersonName.text?.let { it1 ->
                    binding.editTextTextPersonName.setSelection(
                        it1.length
                    )
                }
            }
        }
        binding.openDownloads.setOnClickListener {
            binding.downloadingItem.visibility=View.GONE
            binding.downloaded.visibility=View.GONE
            clicked=true
            startActivity(Intent(this, DownloadsActivity::class.java))
            this.finish()
            this.overridePendingTransition(0, 0)
        }

    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true

    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        Log.v("AppState", "" + lifecycle.currentState)
        super.onWindowFocusChanged(hasFocus)
        val autoCopy = StorageSharedPref.get("auto_copy_new")
        if (autoCopy != "0") {
            if (hasFocus && !wasPasted) {
                fetchCopyTextFun()
            }
        }

        Log.v("AppState", "" + lifecycle.currentState)
    }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }

    private fun fetchCopyTextFun() {
        val clipboard: ClipboardManager? =
            this?.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager?
        clipboard?.clipboardText()
    }

    private fun ClipboardManager.clipboardText() {
        primaryClip?.apply {
            if (itemCount > 0)
                getItemAt(0)?.text?.let {
                    if (Patterns.WEB_URL.matcher(it.toString()).matches()) {
                        if (StorageSharedPref.get("clipboard") != it.toString()) {
                            StorageSharedPref.save("clipboard", it.toString())
                            if (it.toString().isNotEmpty())
                                binding?.editTextTextPersonName?.setText(it.toString().trim())
//                                binding?.btnDownloadConstraint?.performClick()
                            //   Toast.makeText(this@DownloadLinkActivity,"Pasted",Toast.LENGTH_LONG).show()
//                            YoYo.with(Techniques.Shake)
//                                .duration(800)
//                                .repeat(1)
//                                .playOn(binding.button)
                        } else {
//                            YoYo.with(Techniques.Shake)
//                                .duration(800)
//                                .repeat(1)
//                                .playOn(binding.button)
                            //      Toast.makeText(this@DownloadLinkActivity,"No Pasted",Toast.LENGTH_LONG).show()
                        }
                    }
                }
        }
    }



    override fun onBackPressed() {
        if (config.dashboard_Activity_on_off.value == "on") {
            finish()
        } else {
            exit()
        }

//        startAdActivity(Intent(this, DashboardActivity::class.java), RemoteKeys.tab_back_inters, this)
    }


    fun exit() {
        val dialog = ExitDialog(this)
        Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(
            ColorDrawable(
                Color.TRANSPARENT
            )
        )
        dialog.setCanceledOnTouchOutside(true)
        dialog.show()
        dialog.setOnDismissListener {

        }
        val window: Window? = dialog.window
        window?.setLayout(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
    }





    override fun onDownloadClick(listenerFromLoadInter: () -> Unit) {
//        loadInter(){
//            listenerFromLoadInter.invoke()
//        }
    }





}