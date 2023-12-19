package com.vid.videodownloader.billing


import android.content.Intent
import android.util.Log
import androidx.lifecycle.LifecycleCoroutineScope
import com.android.billingclient.api.*
import com.vid.videodownloader.BuildConfig
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.PremiumActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference


class BillingClientStateListenerImpl internal constructor(val defaultScope: CoroutineScope, activity: PremiumActivity?) :
    BillingClientStateListener {
    private val activityRef: WeakReference<PremiumActivity> = WeakReference(activity)
    private val billingRef: WeakReference<BillingClient> = WeakReference(activityRef.get()?.billingClient)

    var week = "one_week_id"
    var month = "one_month_id"
    var threeMonth = "three_month_id"
    private var skuList : ArrayList<String> = arrayListOf(week,month,threeMonth)
    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            queryAvaliableProducts()
            defaultScope.launch {
                refreshProducts()
            }
        }
    }
    private suspend fun refreshProducts() {
        val purchasesResult = billingRef.get()?.queryPurchasesAsync(BillingClient.SkuType.SUBS)
        val billingResult = purchasesResult?.billingResult
        if (billingResult != null) {
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                processPurchaseList(purchasesResult.purchasesList)
            }
        }
    }

    private fun processPurchaseList(purchases: List<Purchase>?) {
        if (null != purchases) {
            for (purchase in purchases) {
                val purchaseState = purchase.purchaseState
                if (purchaseState == Purchase.PurchaseState.PURCHASED) {
                    defaultScope.launch {
                        if (!purchase.isAcknowledged) {
                            // acknowledge everything --- new purchases are ones not yet acknowledged
                            val billingResult = billingRef.get()?.acknowledgePurchase(
                                AcknowledgePurchaseParams.newBuilder()
                                    .setPurchaseToken(purchase.purchaseToken)
                                    .build()
                            )
                            if (billingResult?.responseCode == BillingClient.BillingResponseCode.OK) {
                                onPurchased()
                            }

                        }
                    }
                } else {
                    // make sure the state is set

                }
            }
        }
        else {
            Log.d("translator", "Empty purchase list.")
        }

    }
    override fun onBillingServiceDisconnected() {}
    private fun queryAvaliableProducts() {
//        if(BuildConfig.DEBUG)
//            skuList = arrayListOf("android.test.purchased")

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuList).setType(BillingClient.SkuType.SUBS)
        billingRef.get()?.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
            // Process the result.
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !skuDetailsList.isNullOrEmpty()) {
                for (skuDetails in skuDetailsList) {
                    when (skuDetails.sku) {
//                        "android.test.purchased" -> {
//                            skuDetails?.let {
//                                activityRef.get()?.skuDetailsMonth?.postValue(it)
//                            }
//                            skuDetails?.let {
//                                activityRef.get()?.skuDetailsWeek?.postValue(it)
//                            }
//                            skuDetails?.let {
//                                activityRef.get()?.skuDetails3Months?.postValue(it)
//                            }
//                        }
                        month -> {
                            skuDetails?.let {
                                activityRef.get()?.skuDetailsMonth?.postValue(it)
                            }
                        }
                        week -> {
                            skuDetails?.let {
                                activityRef.get()?.skuDetailsWeek?.postValue(it)
                            }
                        }
                        threeMonth -> {
                            skuDetails?.let {
                                activityRef.get()?.skuDetails3Months?.postValue(it)
                            }
                        }

                    }
                }
            }
        }
    }
    private fun onPurchased(){
        StorageSharedPref.setAppPurchased(true)
        activityRef.get()?.billingClient?.endConnection()
        activityRef.get()?.startActivityPurch()
    }

}