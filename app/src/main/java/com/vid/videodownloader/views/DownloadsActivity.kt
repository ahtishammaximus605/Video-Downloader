package com.vid.videodownloader.views

import android.app.AlertDialog

import android.content.Intent

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import androidx.core.net.toFile

import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager

import com.tonyodev.fetch2.Status
import com.tonyodev.fetch2.fetch.FetchModulesBuilder
import com.tonyodev.fetch2.util.toDownloadInfo
import com.tonyodev.fetch2core.getUniqueId

import com.vid.videodownloader.adapter.DownloadsAdapter

import com.vid.videodownloader.databinding.ActivityDownloadsBinding
import com.vid.videodownloader.databinding.AlertDialogBinding
import com.vid.videodownloader.databinding.DeleteDialogBinding
import com.vid.videodownloader.interfaces.OnItemClick
import com.vid.videodownloader.model.Download
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.FileUtility
import com.vid.videodownloader.utils.FileUtility.Companion.getDuration
import com.vid.videodownloader.utils.FileUtility.Companion.isPackageInstalled
import com.vid.videodownloader.viewmodel.DownloadedFilesViewModel
import com.vid.videodownloader.views.DashboardActivity.Companion.dashboardClick
import com.vid.videodownloader.views.DownloadLinkActivity.Companion.backPressed
import com.vid.videodownloader.views.SplashActivity.Companion.config
import com.vid.videodownloader.views.dialogs.LoadingDialog
import com.vid.videodownloader.views.fragments.NavigationFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


const val groupId = 1245

class DownloadsActivity :
    BaseActivity<ActivityDownloadsBinding>({ ActivityDownloadsBinding.inflate(it) }),
    OnItemClick<Download> {

    var adapter: DownloadsAdapter? = null
    var downloads: ArrayList<Download?>? = ArrayList()
    var reverseList: List<Download?>? = ArrayList()
    private val viewModel: DownloadedFilesViewModel by viewModels()
    private var loadingDialog: LoadingDialog? = null
    private var adisready = "notshowed"
    var isActivityRunning = false
    var count = 0
    var secondLastEle: Download? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DownloadService.vidCount = 0

        viewModel.init(applicationContext)

        type = 3
        if (savedInstanceState == null)
            supportFragmentManager.beginTransaction()
                .add(binding.navigationContent.id, NavigationFragment.newInstance(3))
                .setReorderingAllowed(true)
                .commit()

        adapter = DownloadsAdapter(this)
        binding.rvDownloads.adapter = adapter
        val lytL = LinearLayoutManager(this)
        binding.rvDownloads.layoutManager = lytL

        val showAd = intent?.getStringExtra("showAd")
        loadingDialog = LoadingDialog(this)
        if (showAd == "1") {
            loadInterDashboardButtonsAd()
        }

        DownloadService.downloadsList?.forEach { download ->
            val index = reverseList?.indexOfFirst { d -> d?.dmId?.toInt() == download.id }
            if (download != null) {
                val file = try {
                    download.fileUri.toFile()
                } catch (ex: Exception) {
                    null
                }
                val duration = try {
                    file?.getDuration(applicationContext)
                } catch (ex: Exception) {
                    null
                }
                if (file != null) {
                    if (index != null && index > -1) {
                        val d = reverseList?.get(index)
                        d?.isDownloading = false
                        d?.name = download.fileUri.lastPathSegment
                        d?.path = download.fileUri.toString()
                        d?.size = file.length()
                        d?.duration = duration
                        d?.dmId = download.id.toLong()
                        adapter?.notifyItemChanged(index)
                    } else {
                        val d = convert(download)
                        downloads?.add(d)
                        reverseList = downloads?.asReversed()
                        adapter?.submitList(reverseList)

                    }
                }
            }
            if (downloads.isNullOrEmpty())
                binding.lytEmpty.visibility = View.VISIBLE
            else
                binding.lytEmpty.visibility = View.GONE
        }
        adapter?.submitList(reverseList)
        if (downloads.isNullOrEmpty())
            binding.lytEmpty.visibility = View.VISIBLE
        else
            binding.lytEmpty.visibility = View.GONE

        DownloadService.updated.observe(this, Observer { download ->
            if (download != null) {
                update(download)
            }
        })
        DownloadService.canceled.observe(this, Observer { download ->
            if (download != null) {
                if (reverseList != null)
                    downloads = ArrayList(reverseList!!.asReversed())
                val index = downloads?.indexOfFirst { d -> d?.path == download?.fileUri.toString() }

                if (index != null && index > -1) {
                    downloads?.removeAt(index)
                    reverseList = downloads?.asReversed()
                    adapter?.submitList(reverseList)
                }
                if (downloads.isNullOrEmpty())
                    binding.lytEmpty.visibility = View.VISIBLE
                else
                    binding.lytEmpty.visibility = View.GONE
            }
            adapter?.notifyDataSetChanged()
        })

        DownloadService.completed.observe(this, Observer { download ->
            val file = try {
                download?.fileUri?.toFile()
            } catch (ex: Exception) {
                null
            }
            if (file != null && download != null) {
                completed(download)
            }
        })


    }

    fun convert(download: com.tonyodev.fetch2.Download): com.vid.videodownloader.model.Download {
        val file = download.fileUri.toFile()
        val duration = try {
            file.getDuration(this)
        } catch (ex: Exception) {
            null
        }
        val d = com.vid.videodownloader.model.Download(
            download.fileUri.lastPathSegment,
            download.fileUri.toString(),
            file.length(),
            duration,
            download.id.toLong(),
            false,
            100,
            download.id.toLong(),
            download.tag,
            download.total,
            false
        )
        return d
    }

    fun update(download: com.tonyodev.fetch2.Download?) {
        val index = reverseList?.indexOfFirst { d -> d?.path == download?.fileUri.toString() }
        var p = download?.progress ?: 1
        if (p < -1) p = 1
        if (download != null) {
            if (index != null && index > -1) {
                if (p < 100) {
                    val d = reverseList?.get(index)
                    d?.isDownloading = true
                    d?.progress = p
                    d?.totalbytes = download.total
                    //d?.name = download.fileUri.lastPathSegment
                    //d?.path = download.fileUri.toString()
                    //d?.dmId = download.id.toLong()

                    if (reverseList != null)
                        downloads = ArrayList(reverseList!!.asReversed())

                    adapter?.notifyItemChanged(index)
                }
            } else {
                val d = Download(
                    download.fileUri.lastPathSegment,
                    download.fileUri.toString(),
                    0,
                    0,
                    download.id.toLong(),
                    true,
                    p,
                    download.id.toLong(),
                    download.url,
                    download.total,
                    false
                )
                downloads?.add(d)
                reverseList = downloads?.asReversed()
                adapter?.submitList(reverseList)
            }
        }

        if (downloads.isNullOrEmpty())
            binding.lytEmpty.visibility = View.VISIBLE
        else
            binding.lytEmpty.visibility = View.GONE
        adapter?.notifyDataSetChanged()
//        val lastIndex = reverseList?.count()?: 0
//        val secondLast = lastIndex - 2
//        if(secondLast > 0) {
//            val d = reverseList?.get(secondLast)
//            val s = downloads?.get(1)
//
//           // if(s?.path != d?.path){
//                d?.isDownloading = s?.isDownloading == true
//                d?.isCanceled = s?.isCanceled == true
//                d?.progress = s?.progress?:1
//                d?.totalbytes = s?.totalbytes?:0
//                d?.name = s?.name
//                d?.path = s?.path
//                d?.dmId = s?.dmId?:0
//                d?.id = s?.id
//
//                adapter?.notifyItemChanged(secondLast)
//            //}
//
//        }
    }

    fun completed(download: com.tonyodev.fetch2.Download?) {

        val index = reverseList?.indexOfFirst { d -> d?.path == download?.fileUri.toString() }
        if (download != null) {
            val file = download.fileUri.toFile()
            val duration = try {
                file.getDuration(applicationContext)
            } catch (ex: Exception) {
                null
            }
            if (index != null && index > -1) {
                val d = reverseList?.get(index)
                d?.isDownloading = false
                d?.name = download.fileUri.lastPathSegment
                d?.path = download.fileUri.toString()
                d?.size = file.length()
                d?.duration = duration
                d?.dmId = download.id.toLong()

                if (reverseList != null)
                    downloads = ArrayList(reverseList!!.asReversed())

                adapter?.notifyItemChanged(index)
            } else {
                val d = Download(
                    download.fileUri.lastPathSegment,
                    download.fileUri.toString(),
                    file.length(),
                    duration,
                    download.id.toLong(),
                    false,
                    100,
                    download.id.toLong(),
                    download.tag,
                    download.total,
                    false
                )
                downloads?.add(d)
                reverseList = downloads?.asReversed()
                adapter?.submitList(reverseList)
            }
        }
        if (downloads.isNullOrEmpty())
            binding.lytEmpty.visibility = View.VISIBLE
        else
            binding.lytEmpty.visibility = View.GONE
        adapter?.notifyDataSetChanged()
//        val lastIndex = reverseList?.count()?: 0
//        val secondLast = lastIndex - 2
//        if(secondLast > 0) {
//            val d = reverseList?.get(secondLast)
//            val s = downloads?.get(1)
//
//            // if(s?.path != d?.path){
//            d?.isDownloading = s?.isDownloading == true
//            d?.isCanceled = s?.isCanceled == true
//            d?.progress = s?.progress?:1
//            d?.totalbytes = s?.totalbytes?:0
//            d?.name = s?.name
//            d?.path = s?.path
//            d?.dmId = s?.dmId?:0
//            d?.id = s?.id
//
//            adapter?.notifyItemChanged(secondLast)
//            //}
//
//        }

    }

    override fun itemClickResult(w: Download, name: String) {
        when (name) {
            "onCancel" -> {
                if (reverseList != null)
                    downloads = ArrayList(reverseList!!.asReversed())
                DownloadService.rxFetch?.cancel(w.dmId.toInt())
                DownloadService.rxFetch?.delete(w.dmId.toInt())
                DownloadService.updated.postValue(null)
                DownloadService.canceled.postValue(null)
                DownloadService.completed.postValue(null)

                val index2 = downloads?.indexOfFirst { d -> d?.dmId == w?.dmId }
                val i = DownloadService.downloadsList?.indexOfFirst { s -> s.id == w.dmId.toInt() }
                if (i != null && i > -1) {
                    DownloadService.downloadsList?.removeAt(i)
                }
                if (index2 != null && index2 > -1) {
                    downloads?.removeAt(index2)
                    reverseList = downloads?.asReversed()
                    adapter?.submitList(reverseList)
                }
                if (reverseList.isNullOrEmpty())
                    binding.lytEmpty.visibility = View.VISIBLE
                else
                    binding.lytEmpty.visibility = View.GONE
                adapter?.notifyDataSetChanged()
                //StorageVideos.removeVideo(w.dmId)
            }

            "onRename" -> {
                alertDialogDemo(w.name, w.dmId)
            }

            "onPlay" -> {
                startActivity(
                    Intent(
                        applicationContext,
                        PlayerActivity::class.java
                    ).putExtra("url", w.path.toString()).putExtra("id", w.name)
                )
            }

            "onShare" -> {
                val f: File? = try {
                    Uri.parse(w.path).toFile()
                } catch (ex: java.lang.Exception) {
                    null
                }
                val uri = f?.let {
                    FileProvider.getUriForFile(
                        this,
                        "$packageName.provider",
                        it
                    )
                }
                if (uri != null)
                    startActivity(
                        Intent(
                            applicationContext,
                            ShareActivity::class.java
                        ).putExtra("url", uri.toString())
                    )
            }

            "onRepost" -> {
                if (isPackageInstalled("com.facebook.katana")) {
                    val intent: Intent? =
                        packageManager.getLaunchIntentForPackage("com.facebook.katana")
                    val f: File? = try {
                        Uri.parse(w.path).toFile()
                    } catch (ex: java.lang.Exception) {
                        null
                    }
                    val uri = f?.let {
                        FileProvider.getUriForFile(
                            this,
                            "$packageName.provider",
                            it
                        )
                    }
                    if (uri != null) {

                        if (intent != null) {
                            try {
                                // The application exists
                                val shareIntent = Intent()
                                shareIntent.action = Intent.ACTION_SEND
                                shareIntent.setPackage("com.facebook.katana")
                                shareIntent.putExtra(Intent.EXTRA_TITLE, "Shared Video!")
                                shareIntent.type = "video/*"
                                shareIntent.putExtra(Intent.EXTRA_STREAM, uri)
                                // Start the specific social application
                                startActivity(shareIntent)
                            } catch (ex: Exception) {
                                Toast.makeText(this, "Please Install App!", Toast.LENGTH_LONG)
                                    .show()
                            }
                        }
                    }
                } else Toast.makeText(this, "Please Install App!", Toast.LENGTH_LONG).show()
//            val content = ShareLinkContent.Builder()
//                .setContentUrl(Uri.parse("https://developers.facebook.com"))
//                .build()

            }

            "onMirror" -> {
                startActivity(Intent(this, CastActivity::class.java))
            }

            "onDelete" -> {
                deleteDialogDemo(w)
            }

            else ->
                startActivity(
                    Intent(
                        applicationContext,
                        PlayerActivity::class.java
                    ).putExtra("url", w.path.toString())
                )
        }
    }

    private fun deleteDialogDemo(w: Download) {

        // create an alert builder
        val builder = AlertDialog.Builder(this)
        val customLayout = DeleteDialogBinding.inflate(layoutInflater, null, false)
        builder.setView(customLayout.root)
        val dialog = builder.create()
        customLayout.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        customLayout.btnDelete.setOnClickListener {
            DownloadService.rxFetch?.delete(w.dmId.toInt())
            val i = DownloadService.downloadsList?.indexOfFirst { s -> s.id == w.dmId.toInt() }
            if (i != null && i > -1) {
                DownloadService.downloadsList?.removeAt(i)
            }

            DownloadService.updated.postValue(null)
            DownloadService.canceled.postValue(null)
            DownloadService.completed.postValue(null)

            if (reverseList != null)
                downloads = ArrayList(reverseList!!.asReversed())
            val index2 = downloads?.indexOfFirst { d -> d?.dmId == w.dmId }
            if (index2 != null && index2 > -1) {
                downloads?.removeAt(index2)
                reverseList = downloads?.asReversed()
                adapter?.submitList(reverseList)
            }
            if (reverseList.isNullOrEmpty())
                binding.lytEmpty.visibility = View.VISIBLE
            else
                binding.lytEmpty.visibility = View.GONE
            adapter?.notifyDataSetChanged()
            dialog.dismiss()
        }
        dialog.show()


    }

    private fun alertDialogDemo(name1: String?, dmId: Long) {

        // create an alert builder
        val builder = AlertDialog.Builder(this)
        val customLayout = AlertDialogBinding.inflate(layoutInflater, null, false)
        builder.setView(customLayout.root)
        val dialog = builder.create()
        customLayout.nameTextField.setText(name1)
        customLayout.btnDelete.setOnClickListener {
            val nameFile = customLayout.nameTextField.text.toString()
            if(nameFile.length>0)
            {
                val id = dmId.toInt()
                DownloadService.rxFetch?.getDownload(id) {
                    Log.v("", "")
                    val namePre = it?.fileUri?.lastPathSegment?.let { it1 -> getBaseName(it1) }

                    val name = namePre?.let { it1 -> it.file.replace(it1, nameFile) }

                    val f: File? = try {
                        it?.fileUri?.toFile()
                    } catch (ex: java.lang.Exception) {
                        null
                    }
                    if (f != null) {

                        lifecycleScope.launch(Dispatchers.IO) {

                            val fetchDatabaseManagerWrapper =
                                DownloadService.rxFetch?.fetchConfiguration?.let { it1 ->
                                    FetchModulesBuilder.buildModulesFromPrefs(
                                        it1
                                    ).fetchDatabaseManagerWrapper
                                }

                            val download = it?.id?.let { it1 -> fetchDatabaseManagerWrapper?.get(it1) }
                            withContext(Dispatchers.Main) {
                                dialog.dismiss()
                            }
                            if (download?.status != Status.COMPLETED) {
                                withContext(Dispatchers.Main) {
                                    dialog.dismiss()
                                }
                            }
                            val downloadWithFile = name?.let { it1 ->
                                fetchDatabaseManagerWrapper?.getByFile(
                                    it1
                                )
                            }
                            if (downloadWithFile != null) {
                                withContext(Dispatchers.Main) {
                                    dialog.dismiss()
                                }
                            }
                            val copy = fetchDatabaseManagerWrapper?.getNewDownloadInfoInstance()
                                ?.let { it1 -> download?.toDownloadInfo(it1) }
                            copy?.id = download?.url?.let { it1 ->
                                name?.let { it2 ->
                                    getUniqueId(
                                        it1,
                                        it2
                                    )
                                }
                            }!!
                            if (name != null) {
                                copy?.file = name
                            }
                            try {
                                val pair = copy?.let { it1 -> fetchDatabaseManagerWrapper.insert(it1) }
                                if (!pair?.second!!) {
                                    withContext(Dispatchers.Main) {
                                        dialog.dismiss()
                                    }
                                }
                                val renamed = nameFile.let { it1 -> FileUtility().renameFile(f, it1) }
                                if (!renamed) {
                                    fetchDatabaseManagerWrapper.delete(copy)
                                    withContext(Dispatchers.Main) {
                                        dialog.dismiss()
                                    }
                                } else {
                                    fetchDatabaseManagerWrapper.delete(download)
                                    val d2 = pair.first
                                    val index = reverseList?.indexOfFirst { d -> d?.dmId == dmId }
                                    val index2 = downloads?.indexOfFirst { d -> d?.dmId == dmId }
                                    val i =
                                        DownloadService.downloadsList?.indexOfFirst { s -> s.id == id }
                                    if (index2 != null && index2 > -1) {
                                        if (i != null) {
                                            DownloadService.downloadsList?.removeAt(i)
                                            DownloadService.downloadsList?.add(d2)
                                            DownloadService.updated.postValue(null)
                                            DownloadService.canceled.postValue(null)
                                            DownloadService.completed.postValue(null)
                                        }
                                        downloads?.get(index2)?.name = d2.fileUri.lastPathSegment
                                        downloads?.get(index2)?.path = d2.fileUri.toString()
                                        reverseList = downloads?.asReversed()
                                        withContext(Dispatchers.Main) {
                                            if (index != null && index > -1)
                                                adapter?.notifyItemChanged(index)
                                            dialog.dismiss()
                                        }
                                    }

                                }
                            } catch (e: Exception) {
                                runOnUiThread {
                                    Toast.makeText(
                                        applicationContext,
                                        "Name already exists!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }

                                e.printStackTrace()

                            }
                        }

                    } else {
                        Toast.makeText(
                            this@DownloadsActivity,
                            "SomeThing Went wrong!",
                            Toast.LENGTH_LONG
                        ).show()
                        dialog.dismiss()
                    }

                }
            }
            else{
                Toast.makeText(this, "Please enter video name", Toast.LENGTH_SHORT).show()
            }


        }
        dialog.show()


    }

    fun getBaseName(fileName: String): String {
        val index = fileName.lastIndexOf('.')
        return if (index == -1) {
            fileName
        } else {
            fileName.substring(0, index)
        }
    }

    override fun onBackPressed() {
        if (SplashActivity.config.dashboard_Activity_on_off.value == "on") {
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            startActivity(Intent(this, DownloadLinkActivity::class.java))
            overridePendingTransition(0,0)
        }
    }


    override fun onResume() {
        super.onResume()
        isActivityRunning = true
        if (isActivityRunning) {
            showinterad()
        }
        if (!DownloadService.isStarted)
            startService(Intent(this, DownloadService::class.java))

    }

    override fun onPause() {
        super.onPause()
        isActivityRunning = false
    }



    private fun loadInterDashboardButtonsAd() {
        showinterad()
    }

    private fun showinterad() {
        if (dashboardClick && adisready == "notshowed" ) {

        }

    else if (playerActivityBack ) {
                    playerActivityBack=false

    }
        else if (backPressed ) {
            adisready="notshowed"
                        backPressed=false
        }

}

}