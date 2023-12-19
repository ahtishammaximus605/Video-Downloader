package com.vid.videodownloader.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder

import androidx.core.app.NotificationCompat
import androidx.core.net.toFile
import androidx.lifecycle.MutableLiveData
import com.tonyodev.fetch2.*

import com.tonyodev.fetch2core.DownloadBlock
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import com.vid.videodownloader.BuildConfig
import com.vid.videodownloader.R
import com.vid.videodownloader.background.DownloadRep

import com.vid.videodownloader.views.PlayerActivity
import com.vid.videodownloader.views.groupId
import kotlinx.coroutines.*
import java.lang.IllegalArgumentException

import kotlin.math.abs

class DownloadService : Service() {
    var rep : DownloadRep? = null
    var dm : DownloadManager? = null
    var pIds : List<String>? = null
    var job: Job? = null
    var url : String? = null
    var pIdsTemp : String? = ""

    companion object {
        const val STAT_PLAY = "playing"
        const val STAT_PAUSE = "pause"
        val videoDownloadedLiveData = MutableLiveData<String>()
        val videoDownloadStartedLiveData = MutableLiveData<String>()
        var vidCount = 0
        var isStarted = false
        var rxFetch : Fetch? = null

        var downloadsList : ArrayList<Download>? = ArrayList()
        var downloads : MutableLiveData<List<Download>?> = MutableLiveData()
        var updated : MutableLiveData<Download?> = MutableLiveData()
        var canceled : MutableLiveData<Download?> = MutableLiveData()
        var completed : MutableLiveData<Download?> = MutableLiveData()

    }

    override fun onDestroy() {
        super.onDestroy()
        isStarted = false
        rxFetch?.removeListener(fetchListener)
        rxFetch?.close()
    }
    var notificationManager : NotificationManager? = null
    private fun initDownloader(){
        if(rxFetch == null || rxFetch?.isClosed == true) {

            val fetchConfiguration: FetchConfiguration = FetchConfiguration.Builder(applicationContext)
                .setDownloadConcurrentLimit(10)
                .setNamespace("videodwldr")
                .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
                .build()
            Fetch.setDefaultInstanceConfiguration(fetchConfiguration)

            rxFetch = Fetch.getInstance(fetchConfiguration)

            rxFetch?.getDownloadsInGroup(groupId)  { downloads1->
                downloads.postValue(downloads1.toMutableList())
                if(downloads1.isNotEmpty())
                downloadsList?.addAll(downloads1)

            }?.addListener(fetchListener)
        }
    }


    private val fetchListener =  object: FetchListener {
        override fun onAdded(download: com.tonyodev.fetch2.Download) {
            updated.postValue(download)
        }
        override fun onCancelled(download: com.tonyodev.fetch2.Download) {
            canceled.postValue(download)
        }
        override fun onCompleted(download: com.tonyodev.fetch2.Download) {
            try {
                val file = download.fileUri.toFile()
                if (download.status == Status.COMPLETED
                    && file.length() > 0
                    && file.exists()
                ) {
                    val d = downloadsList?.firstOrNull { s->s.id == download.id }
                    if(d == null)
                    downloadsList?.add(download)
                    completed.postValue(download)
                    videoDownloadedLiveData.postValue("1")

                    val id = abs(download.id)

                    val notifyIntent = Intent(this@DownloadService, PlayerActivity::class.java)
                        .putExtra("url", download.fileUri.toString())
                        .putExtra("service", "1")
                        .putExtra("id", id.toString())
                    val notifyPendingIntent = PendingIntent.getActivity(
                        this@DownloadService, id, notifyIntent, PendingIntent.FLAG_IMMUTABLE
                    )

                    val channelId = BuildConfig.APPLICATION_ID
                    val defaultSoundUri = RingtoneManager.getDefaultUri(
                        RingtoneManager.TYPE_NOTIFICATION
                    )
                    val notification = NotificationCompat
                        .Builder(this@DownloadService, channelId)
                        .setSmallIcon(R.drawable.facebook_ic)
                        .setContentTitle("${download.fileUri.lastPathSegment?.uppercase()} Download Completed")
                        .setContentText("Tap here to Play Video!")
                        .setSound(defaultSoundUri)
                        .setContentIntent(notifyPendingIntent)
                        //.setOnlyAlertOnce(true)
                        .setAutoCancel(true)
                        .build()
                    notificationManager = getSystemService(
                        NOTIFICATION_SERVICE
                    ) as NotificationManager
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            channelId,
                            "Video Downloader Channel",
                            NotificationManager.IMPORTANCE_DEFAULT
                        )
                        notificationManager!!.createNotificationChannel(channel)
                    }
                    notificationManager!!.notify(id, notification)

                    //StorageVideos.setVideo(convert(download,applicationContext))

                }
                updated.postValue(null)
                canceled.postValue(null)
                completed.postValue(null)
            }
            catch (ex : IllegalArgumentException){}
        }
        override fun onDeleted(download: com.tonyodev.fetch2.Download) {
            canceled.postValue(download)
        }

        override fun onDownloadBlockUpdated(
            download: com.tonyodev.fetch2.Download,
            downloadBlock: DownloadBlock,
            totalBlocks: Int
        ) {
            updated.postValue(download)
        }
        override fun onError(
            download: com.tonyodev.fetch2.Download,
            error: Error,
            throwable: Throwable?
        ) {
            canceled.postValue(download)
        }
        override fun onPaused(download: com.tonyodev.fetch2.Download) {
            updated.postValue(download)
        }
        override fun onProgress(
            download: com.tonyodev.fetch2.Download,
            etaInMilliSeconds: Long,
            downloadedBytesPerSecond: Long
        ) {
            updated.postValue(download)
        }

        override fun onQueued(download: com.tonyodev.fetch2.Download, waitingOnNetwork: Boolean) {
            updated.postValue(download)
        }

        override fun onRemoved(download: com.tonyodev.fetch2.Download) {
            canceled.postValue(download)
        }

        override fun onResumed(download: com.tonyodev.fetch2.Download) {
            updated.postValue(download)
        }

        override fun onStarted(
            download: com.tonyodev.fetch2.Download,
            downloadBlocks: List<DownloadBlock>,
            totalBlocks: Int
        ) {
            updated.postValue(download)
        }

        override fun onWaitingNetwork(download: com.tonyodev.fetch2.Download) {
            updated.postValue(download)
        }

    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        isStarted = true
        initDownloader()
        return super.onStartCommand(intent, flags, startId)

    }
    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

}