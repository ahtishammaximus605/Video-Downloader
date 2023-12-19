package com.vid.videodownloader.views

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.permissionx.guolindev.PermissionX
import com.vid.videodownloader.databinding.ActivityPermissionBinding
import com.vid.videodownloader.model.RemoteKeys
import com.vid.videodownloader.services.DownloadService

class PermissionActivity : BaseActivity<ActivityPermissionBinding>({ ActivityPermissionBinding.inflate(it) }) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lytInterProgress = binding.lytInterProgress

        adLyt1 = binding.lytBanner
        adContent1 = binding.adView
        adProgressBar1 = binding.progressBar7

        adLyt= binding.adLyt
        adProgressBar= binding.adProgressBar
        adContent= binding.adContent

        binding.btnAllow.setOnClickListener {
              if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                  PermissionX.init(this)
                      .permissions(
                          Manifest.permission.READ_EXTERNAL_STORAGE
                      )
                      .explainReasonBeforeRequest()
                      .request { allGranted, _, _ ->
                          if (allGranted) {
                              if(DownloadService.rxFetch?.isClosed == true || DownloadService.rxFetch == null){
                                  stopService(Intent(this, DownloadService::class.java))
                                  startService(Intent(this, DownloadService::class.java))
                              }
                              if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
                              {
                                  startActivity(Intent(this, DashboardActivity::class.java))
                                  finish()
                              }
                              else{
                                  finish()
                              }
                          } else Toast.makeText(this, "Needs Permission!", Toast.LENGTH_LONG).show()
                      }
              }
              else{
                  PermissionX.init(this)
                      .permissions(
                          Manifest.permission.READ_EXTERNAL_STORAGE,
                          Manifest.permission.WRITE_EXTERNAL_STORAGE
                      )
                      .explainReasonBeforeRequest()
                      .request { allGranted, _, _ ->
                          if (allGranted) {
                              if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
                              {
                                  startActivity(Intent(this, DashboardActivity::class.java))
                                  finish()
                              }
                              else{
                                  finish()
                              }
                          } else Toast.makeText(this, "Needs Permission!", Toast.LENGTH_LONG).show()
                      }
              }
          }


    }
}