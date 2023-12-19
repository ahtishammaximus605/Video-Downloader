package com.vid.videodownloader.views

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.Window
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import com.tonyodev.fetch2core.isNetworkAvailable
import com.vid.videodownloader.MyApp
import com.vid.videodownloader.R

import com.vid.videodownloader.databinding.ActivityDashboardBinding

import com.vid.videodownloader.views.SplashActivity.Companion.config
import com.vid.videodownloader.views.dialogs.ExitDialog
import com.vid.videodownloader.views.dialogs.LoadingDialog
import com.vid.videodownloader.views.dialogs.PremiumDialog
import com.vid.videodownloader.views.dialogs.dialogInterface
import java.util.*

class DashboardActivity :
    BaseActivity<ActivityDashboardBinding>({ ActivityDashboardBinding.inflate(it) }) {
    private var loadingDialog: LoadingDialog? = null
    var isActivityRunning = true
    private var adisready = "notshowed"
    private var baseUrlAnything = ""
    private var baseUrlLink = ""
    companion object{
        @Volatile
       var  dashboardClick= false
    }
    val startForResult2 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                startActivity(nextIntent)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lytInterProgress = binding.lytInterProgress

        if (config.base_url_anything.value.isNotEmpty()) {
            baseUrlAnything = "https://thinkshot.site/"
        } else {
            baseUrlAnything = "https://thinkshot.site/"
        }

        if (config.base_url_link.value.isNotEmpty()) {
            baseUrlLink = config.base_url_link.value
        } else {
            baseUrlLink = "https://thinkshot.site/"
        }


        loadingDialog = LoadingDialog(this)



        binding.btnPremium.setOnClickListener {
            //startActivity(Intent(this, PremiumActivity::class.java))
            val dialog = PremiumDialog(this)
            dialog.dg = object : dialogInterface {
                override fun onClose() {
                    if (application is MyApp)
                        (application as MyApp).purchase(this@DashboardActivity)
                }
            }
            Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
            dialog.setCanceledOnTouchOutside(true)
            dialog.show()
            val window: Window? = dialog.window
            window?.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

        }
        binding.imMenu.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.more_app))
                    )
                )
            } catch (e: Exception) {
            }
//            startAdActivity(Intent(this, SettingsActivity::class.java), RemoteKeys.settings_tab_click_inters)
        }

        binding.constraintLayout1347.setOnClickListener {
            val intent = Intent(this, DownloadLinkActivity::class.java)
            intent.putExtra("showAd", "1")
            dashboardClick=true
            startActivity(intent)
//            startAdActivity(Intent(this, DownloadLinkActivity::class.java), RemoteKeys.how_to_download_dash_click_inters)
        }
        binding.constraintLayout146.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("showAd", "1")
            dashboardClick=true
            startActivity(intent)
//            startAdActivity(Intent(this, MoviesActivity::class.java), RemoteKeys.movies_dash_inters)
        }
        binding.constraintLayout134.setOnClickListener {
            val intent = Intent(this, DownloadsActivity::class.java)
            intent.putExtra("showAd", "1")
            dashboardClick=true
            startActivity(intent)
//            startAdActivity(Intent(this, DownloadsActivity::class.java), RemoteKeys.downloads_dash_inters)
        }
        binding.constraintLayout14.setOnClickListener {
            val intent = Intent(this, CastActivity::class.java)
            intent.putExtra("showAd", "1")
            dashboardClick=true
            startActivity(intent)
//            startAdActivity(Intent(this, CastActivity::class.java), RemoteKeys.cast_dash_inters)
        }
//        if(StorageSharedPref.isAppPurchased() == true){
//            binding.constraintLayout.visibility = View.GONE
//        }


        //InterstitialAds.loadInterstitialFacebook(this,StorageSharedPref.get(RemoteIds.inters_id))
    }


    override fun onBackPressed() {
        exit()
    }

    val startForResult3 =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                exit()
            }
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

    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        if(isNetworkAvailable())
        {
        if (intent.getStringExtra("from") == "splash"&& config.admob_splash_InterAd.value=="on") {

        }
        }
        else{
            binding.dashboardNative.visibility=View.GONE
            binding.collapseAbleBanner.visibility=View.GONE
        }


    }




    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }



}