package com.vid.videodownloader.views

import android.Manifest
import android.app.ActivityOptions
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetails
import com.google.android.gms.ads.*
import com.vid.videodownloader.billing.BillingClientStateListenerImpl
import com.vid.videodownloader.billing.PurchasesUpdatedListenerImpl
import com.vid.videodownloader.databinding.ActivityPremiumBinding
import com.vid.videodownloader.interfaces.InterAdCallBack
import com.vid.videodownloader.model.RemoteIds
import com.vid.videodownloader.model.RemoteKeys
import com.vid.videodownloader.utils.StorageSharedPref

class PremiumActivity : BaseActivity<ActivityPremiumBinding>({ ActivityPremiumBinding.inflate(it) }) {
    private var isBack = false
    var billingClient: BillingClient?=null

    var skuDetailsMonth: MutableLiveData<SkuDetails?> = MutableLiveData()
    var skuDetailsWeek: MutableLiveData<SkuDetails?> = MutableLiveData()
    var skuDetails3Months: MutableLiveData<SkuDetails?> = MutableLiveData()

    var from : String? = ""
    var selectedSku :SkuDetails? = null
    var selected  = 1

    var name : String? = null
    lateinit var  purchasesUpdatedListener: PurchasesUpdatedListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lytInterProgress = binding.lytInterProgress
        from = intent?.extras?.getString("from","")
        name = intent?.extras?.getString("nextActName")
        adLyt1 = binding.lytBanner
        adContent1 = binding.adView
        adProgressBar1 = binding.progressBar7

        purchasesUpdatedListener = PurchasesUpdatedListenerImpl(this)
        binding.btnContinueWithAds.setOnClickListener {
            onBackPressed()
        }
        binding.imageView24.setOnClickListener {
            onBackPressed()
        }
        binding.lytPurchase.setOnClickListener {
            if(StorageSharedPref.isNetworkAvailable(this)) {
                when (selected) {
                    1 -> selectedSku = skuDetailsMonth.value
                    2 -> selectedSku = skuDetailsWeek.value
                    3 -> selectedSku = skuDetails3Months.value
                }
                if (selectedSku != null) {
                    val billingFlowParams = BillingFlowParams.newBuilder()
                    billingFlowParams.setSkuDetails(selectedSku!!)
                    billingClient?.launchBillingFlow(this, billingFlowParams.build())?.responseCode
                }
            }
            else Toast.makeText(this, "No internet!", Toast.LENGTH_LONG).show()
        }
        binding.btnContinueWithAds.visibility = View.INVISIBLE
        binding.imageView24.visibility = View.INVISIBLE
        Handler(Looper.myLooper()!!).postDelayed({
            binding.btnContinueWithAds.visibility = View.VISIBLE
            binding.imageView24.visibility = View.VISIBLE
            isBack = true
        }, 3000)

        setUpBillingClient()

        skuDetailsMonth.observe(this, Observer {
            val p = it?.price
            val c = it?.priceCurrencyCode
            binding.txtpricemonth.text = "$c $p"
        })
        skuDetailsWeek.observe(this, Observer {
            val p = it?.price
            val c = it?.priceCurrencyCode
            binding.txtpriceweek.text = "$c $p"
        })
        skuDetails3Months.observe(this, Observer {
            val p = it?.price
            val c = it?.priceCurrencyCode
            binding.txtpricemonth3.text = "$c $p"
        })
        binding.imageView211.setOnClickListener {
            selected = 1
            unselect()
        }
        binding.imageView21.setOnClickListener {
            selected = 2
            unselect()
        }
        binding.imageView2113.setOnClickListener {
            selected = 3
            unselect()
        }

        //InterstitialAds.loadInterstitialFacebook(this,StorageSharedPref.get(RemoteIds.inters_id))


    }
    private fun unselect(){

        binding.imageView211.setImageResource(com.vid.videodownloader.R.drawable.gold)
        binding.imageView21.setImageResource(com.vid.videodownloader.R.drawable.gold)
        binding.imageView2113.setImageResource(com.vid.videodownloader.R.drawable.gold)
        binding.imageView211.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.silver,theme)
        binding.imageView21.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.silver,theme)
        binding.imageView2113.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.silver,theme)

        binding.txtpriceweek.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView40.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView45.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))

        binding.txtpricemonth.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView401.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView451.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))

        binding.txtpricemonth3.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView4013.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))
        binding.textView4513.setTextColor(resources.getColor(com.vid.videodownloader.R.color.silver,theme))

        when (selected) {
            1 -> {
                binding.imageView211.setImageResource(com.vid.videodownloader.R.drawable.bg_gold1)
                binding.imageView211.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.gold,theme)
                binding.txtpricemonth.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView401.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView451.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))

            }
            2 -> {
                binding.imageView21.setImageResource(com.vid.videodownloader.R.drawable.bg_gold1)
                binding.imageView21.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.gold,theme)
                binding.txtpriceweek.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView40.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView45.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
            }
            3 -> {
                binding.imageView2113.setImageResource(com.vid.videodownloader.R.drawable.bg_gold1)
                binding.imageView2113.imageTintList = resources.getColorStateList(com.vid.videodownloader.R.color.gold,theme)
                binding.txtpricemonth3.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView4013.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
                binding.textView4513.setTextColor(resources.getColor(com.vid.videodownloader.R.color.black,theme))
            }
        }

    }
    override fun onBackPressed() {

        val remote = if(from == "splash") RemoteKeys.prem_splash_inters else RemoteKeys.prem_inters
        if(isBack) {
            if(name != null){
                setResult(RESULT_OK)
                finish()
            }
            else {
                binding.imageView24.visibility = View.INVISIBLE

                            startActivity()
            }
        }
    }
    fun startActivity(){
        billingClient?.endConnection()
        when {
            from == "splash" -> {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        // You can use the API that requires the permission.
                          if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
            {
                startActivity(Intent(this, DashboardActivity::class.java))
            }
                    }
                    else ->{
                        startActivity(Intent(applicationContext, PermissionActivity::class.java))
                        finish()
                    }
                }

            }
//            name != null -> {
//                startActivity(next)
//                finish()
//            }
            else -> {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
    fun startActivityPurch(){
        billingClient?.endConnection()
        when {
            from == "splash" -> {
                when (PackageManager.PERMISSION_GRANTED) {
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) -> {
                        // You can use the API that requires the permission.
                          if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
            {
                startActivity(Intent(this, DashboardActivity::class.java))
            }
                    }
                    else ->{
                        startActivity(Intent(applicationContext, PermissionActivity::class.java))
                        finish()
                    }
                }

            }
            name != null -> {
                setResult(RESULT_OK)
                finish()
            }
            else -> {
                if(SplashActivity.config.dashboard_Activity_on_off.value=="on")
                {
                    startActivity(Intent(this, DashboardActivity::class.java))
                    finish()
                }
                else{
                    finish()
                }
            }
        }
    }
    private fun setUpBillingClient() {
        billingClient = BillingClient.newBuilder(applicationContext)
            .enablePendingPurchases()
            .setListener(purchasesUpdatedListener)
            .build()
        startConnection()
    }

    private fun startConnection() {
        billingClient?.startConnection(BillingClientStateListenerImpl(lifecycleScope,this))
    }
}