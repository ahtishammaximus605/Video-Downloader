package com.vid.videodownloader.views

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.core.content.FileProvider
import androidx.lifecycle.Observer
import com.vid.videodownloader.BuildConfig
import com.vid.videodownloader.databinding.ActivityDownloadingStateBinding
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.views.DownloadLinkActivity.Companion.backPressed
import com.vid.videodownloader.views.DownloadLinkActivity.Companion.errorDownloading
import com.vid.videodownloader.views.DownloadLinkActivity.Companion.videoAlready
import com.vid.videodownloader.views.dialogs.LoadingDialog
import java.io.File

class DownloadingStateActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDownloadingStateBinding
    private var adisready = "notshowed"
    private var isActivityRunning: Boolean = false

    private var loadingDialog: LoadingDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDownloadingStateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog = LoadingDialog(this)

            DownloadLinkActivity.workAfterAd?.invoke()
        errorDownloading = {errorTitle, error ->
            binding.videoAlready.text = error
            binding.videoAlreadyTitle.text=errorTitle
            binding.videoAlready.visibility = View.VISIBLE
            binding.videoAlreadyTitle.visibility = View.VISIBLE
            binding.goToDownloads.visibility = View.GONE
            binding.downloaded.visibility = View.INVISIBLE
            binding.dramaLayout.visibility = View.INVISIBLE

        }

        videoAlready = {
            binding.videoAlready.visibility = View.VISIBLE
            binding.videoAlreadyTitle.visibility = View.VISIBLE
            binding.goToDownloads.visibility = View.VISIBLE
            binding.downloaded.visibility = View.INVISIBLE
            binding.dramaLayout.visibility = View.INVISIBLE
        }
        binding.goToDownloads.setOnClickListener {
            startActivity(Intent(this, DownloadsActivity::class.java))
            this.finish()
            this.overridePendingTransition(0, 0)
        }

        DownloadService.updated.observe(this, Observer { download ->
            if (download != null ) {
                if (download.progress in 1..45) {
                    binding.dramaText.text = "Processing"
                } else if (download.progress in 50..90) {
                    binding.dramaText.text = "Downloading"
                    binding.textView14.text = "Please be patient we are downloading your video."
                } else if (download.progress == 100) {
                    binding.downloaded.visibility = View.VISIBLE
                    binding.dramaLayout.visibility = View.INVISIBLE
                    DownloadService.updated.postValue(null)
                    DownloadService.canceled.postValue(null)
                    DownloadService.completed.postValue(null)

                }

            }
        })



        binding.downloaded.setOnClickListener {

            val directoryName = "Download/videodownloader"
            val latestVideo = getLatestVideoFromInternalStorage(this, directoryName)

            if (latestVideo != null) {
                val latestVideoPath = latestVideo.absolutePath
                startActivity(
                    Intent(
                        applicationContext,
                        PlayerActivity::class.java
                    ).putExtra("url", latestVideoPath.toString()).putExtra("id", " w.name")
                )
                this.finish()

            }
        }
        binding.share.setOnClickListener {
            val directoryName = "Download/videodownloader"
            val latestVideo = getLatestVideoFromInternalStorage(this, directoryName)
            if (latestVideo != null) {
                shareVideoWithOtherApps(this, latestVideo)
            } else {
                println("No video files found in the directory.")
            }

        }
        Handler(Looper.getMainLooper()).postDelayed(
            { binding.backPress.visibility = View.VISIBLE },
            3000
        )
        binding.backPress.setOnClickListener {
            onBackPressed()
        }
        binding.downloaded.visibility = View.INVISIBLE
        binding.dramaLayout.visibility = View.VISIBLE

    }


    override fun onBackPressed() {
        //   super.onBackPressed()

        backPressed=true
        startActivity(Intent(this, DownloadsActivity::class.java))
        this.finish()

    }





    override fun onResume() {
        super.onResume()
        isActivityRunning = true
//        binding.downloaded.visibility=View.INVISIBLE
//        binding.dramaLayout.visibility=View.VISIBLE


    }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }

    fun shareVideoWithOtherApps(context: Context, videoFile: File) {

        val link = "http://play.googlee.com/store/apps/details?id=${packageName}"
        val shareMessage =
            "You can download facebook videos for free and fast. \n Download it here: $link".trimIndent()
        val uri =
            FileProvider.getUriForFile(context, "${BuildConfig.APPLICATION_ID}.provider", videoFile)

        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "video/*"
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

        context.startActivity(Intent.createChooser(shareIntent, "Share Video"))

    }

    private fun getLatestVideoFromInternalStorage(context: Context, directoryName: String): File? {
        val internalStorageDir = context.getExternalFilesDir(null)
        val directory = File(internalStorageDir, directoryName)
        if (!directory.exists() || !directory.isDirectory) {

            return null
        }
        val videoFiles = directory.listFiles { file ->
            file.isFile && file.extension.matches(Regex("mp4", RegexOption.IGNORE_CASE))
        }

        if (videoFiles.isNullOrEmpty()) {
            return null
        }
        videoFiles.sortWith(Comparator { file1, file2 ->
            (file2.lastModified() - file1.lastModified()).toInt()
        })

        return videoFiles[0]
    }

    // Usage

}