package com.vid.videodownloader.views

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.google.android.gms.ads.*


import com.vid.videodownloader.databinding.ActivityCastBinding
import com.vid.videodownloader.views.DashboardActivity.Companion.dashboardClick
import com.vid.videodownloader.views.SplashActivity.Companion.config
import com.vid.videodownloader.views.dialogs.LoadingDialog
import com.vid.videodownloader.views.fragments.NavigationFragment


class CastActivity : BaseActivity<ActivityCastBinding>({ ActivityCastBinding.inflate(it) }){

    private var loadingDialog: LoadingDialog? = null
    private var adisready = "notshowed"
    var isActivityRunning = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .add(binding.navigationContent.id, NavigationFragment.newInstance(4))
                .setReorderingAllowed(true)
                .commit()
        lytInterProgress = binding.lytInterProgress

        val showAd = intent?.getStringExtra("showAd")
        loadingDialog = LoadingDialog(this)
        if (showAd == "1") {
            loadInterDashboardButtonsAd()
        }



        binding.button2.setOnClickListener {
            try {
                val intent = Intent("android.settings.CAST_SETTINGS")
                startActivity(intent)
                //finish()
            } catch (exception1: Exception) {
                Toast.makeText(this, "Device not supported", Toast.LENGTH_LONG).show()
            }
        }
    }
    private fun checkDisplay() {
        val displayManager = getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        if (displayManager.displays.size >= 2) {
            binding.button2.text = "Connected"
        } else {
            binding.button2.text = "Connect"
        }
    }

    override fun onBackPressed() {

        if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
        {
            finish()
        }
        else{
            startActivity(Intent(this, DownloadLinkActivity::class.java))
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        checkDisplay()
        if (isActivityRunning) {

        }

    }



    private fun loadInterDashboardButtonsAd() {


    }



    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }


}