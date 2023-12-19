package com.vid.videodownloader

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.widget.Toast
import com.android.billingclient.api.*
import com.google.common.collect.ImmutableList


import com.vid.videodownloader.databinding.PremiumDialogBinding
import com.vid.videodownloader.utils.StorageSharedPref



class MyApp: Application() , Application.ActivityLifecycleCallbacks
 {

    private var currentActivity: Activity? = null
     var billingClient: BillingClient?=null
     lateinit var binding: PremiumDialogBinding
     fun purchase( activity: Activity)
     {
         if(StorageSharedPref.isNetworkAvailable(this)) {
             if (pDetails != null) {
                         if(pDetails != null) {
                            val billingFlowParams = BillingFlowParams.newBuilder()
                                .setProductDetailsParamsList(
                                    ImmutableList.of(
                                        BillingFlowParams.ProductDetailsParams.newBuilder()
                                            // retrieve a value for "productDetails" by calling queryProductDetailsAsync()
                                            .setProductDetails(pDetails!!)
                                            .build()
                                    )
                                )
                                .build()

                            // Launch the billing flow
                            val billingResult = billingClient?.launchBillingFlow(activity, billingFlowParams)

                   }
//                 val flowParams = BillingFlowParams.newBuilder()
//                     .setSkuDetails(selectedSku!!)
//                     .build()
//                 if(billingClient?.isReady == true && billingClient?.connectionState == BillingClient.ConnectionState.CONNECTED)
//                 currentActivity?.let { billingClient!!.launchBillingFlow(it, flowParams).responseCode }
//                 else if(billingClient?.connectionState != BillingClient.ConnectionState.CONNECTED){
//                     billingClient?.endConnection()
//                     billingClient = purchasesUpdatedListener?.let {
//                         BillingClient.newBuilder(this)
//                             .setListener(it)
//                             .enablePendingPurchases()
//                             .build()
//                     }
//                     billingClient?.startConnection(object : BillingClientStateListener {
//                         override fun onBillingSetupFinished(billingResult: BillingResult) {
//                             if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
//                                     currentActivity?.let { billingClient!!.launchBillingFlow(it, flowParams).responseCode }
//                             }
//                         }
//                         override fun onBillingServiceDisconnected() {
//                             // Try to restart the connection on the next request to
//                             // Google Play by calling the startConnection() method.
//                         }
//                     })
//                 }
             }
         }
         else Toast.makeText(currentActivity, "No internet!", Toast.LENGTH_LONG).show()
     }
     var pDetails : ProductDetails? = null
     fun querySkuDetails() {
         val id = if(BuildConfig.DEBUG) "android.test.purchased" else packageName

         val params = QueryProductDetailsParams.newBuilder()
         params.setProductList( ImmutableList.of(
             QueryProductDetailsParams.Product.newBuilder()
                 .setProductId(id)
                 .setProductType(BillingClient.ProductType.INAPP)
                 .build()))

         billingClient?.queryProductDetailsAsync(params.build(), ProductDetailsResponseListener{ billingResult2, productDetailsList ->
             if(productDetailsList.isNotEmpty())
             pDetails = productDetailsList[0]
         })

     }
     private fun handleNonConcumablePurchase(purchase: Purchase) {
         if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
             if (!purchase.isAcknowledged) {
                 val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                     .setPurchaseToken(purchase.purchaseToken).build()
                 billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                     onPurchased()
                 }

             }else{
                 onPurchased()
             }
         }
     }
     private fun onPurchased(){
         StorageSharedPref.setAppPurchased(true)
         billingClient?.endConnection()


     }
     private fun purchaseNotSuccess(){
         StorageSharedPref.setAppPurchased(false)
         billingClient?.endConnection()
         //dismiss()
     }
     var purchasesUpdatedListener :PurchasesUpdatedListener? = null
    override fun onCreate() {
        super.onCreate()




        registerActivityLifecycleCallbacks(this)
        purchasesUpdatedListener =
            PurchasesUpdatedListener { billingResult, purchases ->
                // To be implemented in a later section.
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    for (purchase in purchases) {
                        handleNonConcumablePurchase(purchase)
                    }
                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle an error caused by a user cancelling the purchase flow.
                    purchaseNotSuccess()
                } else {
                    // Handle any other error codes.
                    purchaseNotSuccess()
                }
            }

        billingClient = BillingClient.newBuilder(this)
            .setListener(purchasesUpdatedListener!!)
            .enablePendingPurchases()
            .build()
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    querySkuDetails()
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })




    }
    override fun onActivityPaused(activity: Activity) {

    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityDestroyed(activity: Activity) {

    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

    }

    override fun onActivityStopped(activity: Activity) {


    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        currentActivity = activity

    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }


}