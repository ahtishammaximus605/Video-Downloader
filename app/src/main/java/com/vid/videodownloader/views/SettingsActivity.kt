package com.vid.videodownloader.views

import android.app.ActivityOptions
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.vid.videodownloader.PrivacyPolicyActivity
import com.vid.videodownloader.databinding.SettingsActivityBinding
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.dialogs.FeedbackDialog
import com.vid.videodownloader.views.dialogs.LoadingDialog
import com.vid.videodownloader.views.dialogs.RateDialog
import com.vid.videodownloader.views.fragments.NavigationFragment
import java.util.*

class SettingsActivity : BaseActivity<SettingsActivityBinding>({ SettingsActivityBinding.inflate(it) }){

    private var loadingDialog: LoadingDialog? = null
    private var adisready = "notshowed"
    private var isActivityRunning: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadingDialog = LoadingDialog(this)
        if(savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(binding.navigationContent.id, NavigationFragment.newInstance(5))
                .setReorderingAllowed(true)
                .commit()
        }
        val autoCopy =  StorageSharedPref.get("auto_copy_new")
        if(autoCopy=="0")
        {
            binding.switch1.isChecked=false
        }
        else{
            binding.switch1.isChecked=true
        }

        binding.switch1.setOnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked)
            {
                Log.e("FirstTime", "${StorageSharedPref.get("auto_copy_new")}: ", )
                StorageSharedPref.save("auto_copy_new","12")
            }
            else {
                Log.e("FirstTime", "${StorageSharedPref.get("auto_copy_new")}: ", )
                StorageSharedPref.save("auto_copy_new","0")
            }
        }
        binding.btnMore.setOnClickListener {
            val uri = Uri.parse("https://play.google.com/store/apps/details?id=com.go.translate.all.language.translator.app.free.translation.guru")
            val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
            try {
                startActivity(myAppLinkToMarket)
            } catch (e: ActivityNotFoundException) {
                Toast.makeText(this, "Couldn't find app.", Toast.LENGTH_LONG).show()
            }
        }
        if(StorageSharedPref.isAppPurchased() != true) {
           // binding.constraintLayout.visibility = View.VISIBLE
        }
        binding.btnPremium.setOnClickListener {
//            if(StorageSharedPref.isAppPurchased() != true) {
////                startActivity(Intent(this, PremiumActivity::class.java))
////
//                val dialog = PremiumDialog(this)
//                dialog.dg = object : dialogInterface {
//                    override fun onClose() {
//                        if(application is MyApp)
//                        (application as MyApp).purchase(this@SettingsActivity)
//                    }
//                }
//                Objects.requireNonNull(dialog.window)?.setBackgroundDrawable(
//                    ColorDrawable(
//                        Color.TRANSPARENT
//                    )
//                )
//                dialog.setCanceledOnTouchOutside(true)
//                dialog.show()
//                val window: Window? = dialog.window
//                window?.setLayout(
//                    ConstraintLayout.LayoutParams.MATCH_PARENT,
//                    ConstraintLayout.LayoutParams.WRAP_CONTENT
//                )
//            }
//            else Toast.makeText(
//                this,
//                "Already Purchased!",
//                Toast.LENGTH_SHORT
//            ).show()
        }
        binding.btnQuit.setOnClickListener {
            startActivity(Intent(this, FbBrowserActivity::class.java).putExtra("logout","1"),  ActivityOptions.makeSceneTransitionAnimation(this, binding.root, "navtop").toBundle())
        }
        binding.btnHowToDownload.setOnClickListener {
            startActivity(Intent(this, DownloadLinkActivity::class.java),  ActivityOptions.makeSceneTransitionAnimation(this, binding.root, "navtop").toBundle())
        }
        binding.btnShare.setOnClickListener {
            startActivity(Intent(this, ShareActivity::class.java))
        }
        binding.btnPrivacy.setOnClickListener {
            startActivity(Intent(this, PrivacyPolicyActivity::class.java))
        }
        binding.btnFeedback.setOnClickListener {
            val dialog = FeedbackDialog(this)
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
        binding.btnRate.setOnClickListener {
            val dialog = RateDialog(this)
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
    }
    override fun onBackPressed() {
        if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
        {
            finish()
        }
        else {
            startActivity(Intent(this, DownloadLinkActivity::class.java))
            overridePendingTransition(0,0)
        }
    }
    override fun onResume() {
        super.onResume()
        isActivityRunning=true


    }

    override fun onPause() {
        super.onPause()
        isActivityRunning=false
    }





}