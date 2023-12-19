package com.vid.videodownloader.views

import android.app.Activity
import android.content.Intent

import android.os.Bundle
import android.os.Environment
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.vid.videodownloader.databinding.ActivityPlayerBinding
import com.vid.videodownloader.model.Download

import com.vid.videodownloader.viewmodel.DownloadedFilesViewModel
import com.vid.videodownloader.views.dialogs.LoadingDialog
import kotlinx.coroutines.launch
import java.io.File

import kotlin.collections.ArrayList



class PlayerActivity  :  BaseActivity<ActivityPlayerBinding>({ ActivityPlayerBinding.inflate(it) }) {
    private var loadingDialog: LoadingDialog?=null
    var videoUrl : String? = null
    var player : SimpleExoPlayer? = null
    private val viewModel2: DownloadedFilesViewModel by viewModels()
    var id :String?= null
    var reverseList : List<Download?>? = ArrayList()
    var loaded : MutableLiveData<Boolean> = MutableLiveData()
    var service : String? = null

    val startForResult2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            init()
            val mediaItem: com.google.android.exoplayer2.MediaItem? = videoUrl?.let {
                com.google.android.exoplayer2.MediaItem.fromUri(
                    it
                )
            }
            if (mediaItem != null) {
                player?.setMediaItem(mediaItem)
                player?.prepare()
                player?.playWhenReady = true
            }
        }
    }
    fun startPremiumAd2(intent1: Intent?, finish : Boolean = true){

        init()
        val mediaItem: com.google.android.exoplayer2.MediaItem? = videoUrl?.let {
            com.google.android.exoplayer2.MediaItem.fromUri(
                it
            )
        }
        if (mediaItem != null) {
            player?.setMediaItem(mediaItem)
            player?.prepare()
            player?.playWhenReady = true
        }

    }
    var s = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lytInterProgress = binding.lytInterProgress
        viewModel2.init(applicationContext)
        service = intent.extras?.getString("service")
        videoUrl = intent.extras?.getString("url")
        id = intent.extras?.getString("id")

        player = SimpleExoPlayer.Builder(this).build()
        binding.playerView.setShowNextButton(false)
        binding.playerView.setShowPreviousButton(false)

        binding.playerView.setShowRewindButton(false)
        binding.playerView.setShowFastForwardButton(false)
        binding.playerView.player = player


        loadingDialog?.show()
        if(service == "1") {
            s = 1

                            service = "0"
                            init()
                            val mediaItem: com.google.android.exoplayer2.MediaItem? = videoUrl?.let {
                                com.google.android.exoplayer2.MediaItem.fromUri(
                                    it
                                )
                            }
                            if (mediaItem != null) {
                                player?.setMediaItem(mediaItem)
                                player?.prepare()
                                player?.playWhenReady = true

                            }
                        }
        else init()

    }
    fun init(){
        if(videoUrl == null) finish()

        if(id != null) {
            lifecycleScope.launch {
                val folder =
                    File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.absolutePath.toString() + "/" + "videodownloader")
                val downloads = viewModel2.getFiles(folder)
                reverseList = downloads?.reversed()
                loaded.postValue(true)

            }
            loaded.observe(this, Observer { t ->
                if (t) {

                    reverseList?.forEach {
                        if (it?.path != videoUrl) {
                            val mediaItem: com.google.android.exoplayer2.MediaItem? =
                                it?.path?.let { itt ->
                                    com.google.android.exoplayer2.MediaItem.fromUri(
                                        itt
                                    )
                                }
                            if (mediaItem != null) {
                                player?.addMediaItem(mediaItem)
                            }
                        }
                    }
                    if (reverseList != null) {
                        if (reverseList!!.count() > 1) {
                            if (id != null) {

                                binding.playerView.setShowPreviousButton(true)

                                binding.playerView.setShowNextButton(true)

                            }

                        }
                    }

                    player?.prepare()
                    player?.playWhenReady = true

                }
            })
        }


    }

    override fun onResume() {
        if(service != "1") {



            if (player == null) {
                player = SimpleExoPlayer.Builder(this).build()
                binding.playerView.setShowNextButton(false)
                binding.playerView.setShowPreviousButton(false)

                binding.playerView.setShowRewindButton(false)
                binding.playerView.setShowFastForwardButton(false)
                binding.playerView.player = player
                init()
            }

                val mediaItem: com.google.android.exoplayer2.MediaItem? = videoUrl?.let {
                    com.google.android.exoplayer2.MediaItem.fromUri(
                        it
                    )
                }
                if (mediaItem != null) {
                    player?.setMediaItem(mediaItem)
                    player?.prepare()
                    player?.playWhenReady = true


                }

        }
        super.onResume()
        player?.addListener(object : Player.Listener {

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {

                    loadingDialog?.dismiss()

                }
                if (state == Player.STATE_ENDED) {

                }
            }

            override fun onPlayerError(error: PlaybackException) {

                }


        })


    }




    override fun onPause() {
        player?.pause()
        player?.release()
        player = null
        super.onPause()
    }


    override fun onBackPressed() {
        player?.pause()
        player?.release()
        player = null
        playerActivityBack=true
        startActivity(Intent(this, DownloadsActivity::class.java))
        this.finish()


    }


}