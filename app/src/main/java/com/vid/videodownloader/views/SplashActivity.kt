package com.vid.videodownloader.views

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.vid.videodownloader.R
import com.vid.videodownloader.ads.FirebaseAnalytic
import com.vid.videodownloader.billing.BilingClientStateSplash
import com.vid.videodownloader.billing.PurchasesUpdatedListenerImplSplash
import com.vid.videodownloader.databinding.ActivitySplashBinding
import com.vid.videodownloader.model.RemoteConfig
import com.vid.videodownloader.model.RemoteConfigDate
import com.vid.videodownloader.model.RemoteIds
import com.vid.videodownloader.model.RemoteKeys
import com.vid.videodownloader.model.RemoteValues
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.utils.StorageSharedPref.Companion.isNetworkAvailable
import com.vid.videodownloader.utils.StorageVideos
import com.vid.videodownloader.views.dialogs.LoadingDialog

class SplashActivity : BaseActivity<ActivitySplashBinding>({ ActivitySplashBinding.inflate(it) }) {

    private val taskCount: MutableLiveData<Int> = MutableLiveData(0)
    var billingClient: BillingClient? = null

    companion object {

        val remoteConfig = RemoteConfigDate("com_video_downloader")
        var config = RemoteConfig()
        var adsplashisready: Boolean = false
        var firebaseAnalytic: FirebaseAnalytic? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_VideoDownloader)
        super.onCreate(savedInstanceState)

        firebaseAnalytic = FirebaseAnalytic(this)
        getRemoteConfig()
        initApp()
        startProgress()

        taskCount.observe(this, Observer<Int>() {
            if (it > 1) initAds()
        })
        if (StorageSharedPref.get("first") == "true") {
            Log.e("FirstTime", "${StorageSharedPref.get("first")}: ")


        } else {
            StorageSharedPref.save("auto_copy_new", "0")
            Log.e("FirstTime", "${StorageSharedPref.get("first")}: ")
        }



        binding.btnStart.setOnClickListener {
            adsplashisready = true
            moveNext()
        }
        StorageSharedPref.save("first", "true")

        billingClient = BillingClient.newBuilder(applicationContext)
            .setListener(PurchasesUpdatedListenerImplSplash(this)).enablePendingPurchases().build()
        billingClient?.startConnection(BilingClientStateSplash(lifecycleScope, this))
        try {
            if (!isPaused) this.startService(Intent(this, DownloadService::class.java))
        } catch (ex: IllegalStateException) {
        }
    }

    private fun getRemoteConfig() {
        if (isNetworkAvailable(this)) {
            remoteConfig.getRemoteConfig(this) {
                it?.let {
                    val remoteJson = Gson().toJson(it)
                    config = Gson().fromJson(remoteJson, RemoteConfig::class.java)
                    Log.e("RemoteConfigNew*", "$config")
                    if (config.admob_splash_InterAd.value == "on") {
                        loadInterSplash()
                    }else{
                        moveNext()
                    }

                    if (config.admob_splash_NativeAd_Position.value == "top") {
                    } else {

                    }


                } ?: Log.e("RemoteConfigNew*", "RemoteFail")
            }
        } else {
          moveNext()
            Log.e("RemoteConfigNew*", "RemoteFailNoInternet")

        }
    }

    var isPaused = false
    override fun onResume() {
        isPaused = false
        super.onResume()

    }

    private fun loadInterSplash() {

            moveNext()

    }

    private fun moveNext() {
        if (config.dashboard_Activity_on_off.value == "on") {
            billingClient?.endConnection()
            startActivityDestroyNativeAd(
                Intent(this@SplashActivity, DashboardActivity::class.java).putExtra(
                    "from",
                    "splash"
                )
            )
            finish()
        } else {
            val intent = Intent(this, DownloadLinkActivity::class.java)
            intent.putExtra("showAd", "1")
            startActivity(intent)
            finish()
        }
    }


    override fun onPause() {
        isPaused = true
        super.onPause()
    }


    private fun initAds() {
        if (StorageSharedPref.isAppPurchased() != true && StorageSharedPref.isNetworkAvailable(
                applicationContext
            ) && StorageSharedPref.verifyInstallerId(applicationContext)
        ) {

            val isOn = StorageSharedPref.get(RemoteKeys.openad)

            if (isOn == RemoteValues.inter || isOn == RemoteValues.open || isOn == RemoteValues.applovin) {
                val id12 = StorageSharedPref.get(RemoteIds.s_inters_id)


            }

        }


    }


    private lateinit var timer: CountDownTimer
    var p = 1
    private fun startProgress() {
        binding.roundedProgressBar.max = 100
        binding.roundedProgressBar.setProgress(p, true)
        timer = object : CountDownTimer(10000, 500) {
            override fun onTick(millisUntilFinished: Long) {
                val totalDuration = 10000
                val elapsedTime = totalDuration - millisUntilFinished + 1000
                val progress = ((elapsedTime.toDouble() / totalDuration) * 100).toInt()
                binding.roundedProgressBar.setProgress(progress, true)
            }

            override fun onFinish() {
                cancelProgress()
                binding.splashNativeTop.visibility = View.GONE
                binding.splashNativeBottom.visibility = View.GONE
            }
        }
        timer.start()
    }

    private fun animateEndProgress() {
        binding.roundedProgressBar.setProgress(80, true)
        timer = object : CountDownTimer(250, 250) {
            override fun onTick(millisUntilFinished: Long) {
                val p1 = (((250 - millisUntilFinished).toDouble()) / 250) * 100
                p = ((p + p1).toInt())
                binding.roundedProgressBar.setProgress(p, true)
            }

            override fun onFinish() {
                binding.roundedProgressBar.setProgress(100, true)
                binding.roundedProgressBar.visibility = View.GONE
                //     binding.btnStart.visibility = View.VISIBLE
//                if(admobNativeADNext == null && admobNativeADNext?.headline != null) {
//                    binding.adLyt.visibility = View.GONE
//                }

            }
        }
        timer.start()
    }

    fun cancelProgress() {
        timer.cancel()
        animateEndProgress()
    }

    private fun initApp() {
        StorageSharedPref.setStorage(applicationContext)
        StorageVideos.setStorage(applicationContext)


        FirebaseApp.initializeApp(applicationContext)
        FirebaseMessaging.getInstance().subscribeToTopic(packageName).addOnCompleteListener { }


        if (!isNetworkAvailable(applicationContext) || !StorageSharedPref.verifyInstallerId(
                applicationContext
            ) || StorageSharedPref.isAppPurchased() == true
        ) {
        }
    }


}