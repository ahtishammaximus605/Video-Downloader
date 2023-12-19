package com.vid.videodownloader.billing

import android.content.Intent
import androidx.annotation.Nullable
import com.android.billingclient.api.*
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.DownloadLinkActivity
import com.vid.videodownloader.views.PremiumActivity
import com.vid.videodownloader.views.SplashActivity
import java.lang.ref.WeakReference


class PurchasesUpdatedListenerImpl internal constructor(activity: PremiumActivity?) :
    PurchasesUpdatedListener {
    private val activityRef: WeakReference<PremiumActivity> = WeakReference(activity)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        @Nullable purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handleNonConcumablePurchase(purchase)
            }
        }
        else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            onPurchased()
        }else purchaseNotSuccess()
    }
    private fun handleNonConcumablePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                activityRef.get()?.billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    onPurchased()
                }
            }else{
                onPurchased()
            }
        }
    }
    private fun onPurchased(){
        StorageSharedPref.setAppPurchased(true)
        activityRef.get()?.billingClient?.endConnection()
        activityRef.get()?.startActivityPurch()
    }
    private fun purchaseNotSuccess(){
        StorageSharedPref.setAppPurchased(false)
    }

}
class PurchasesUpdatedListenerImplSplash internal constructor(activity: SplashActivity?) :
    PurchasesUpdatedListener {
    private val activityRef: WeakReference<SplashActivity> = WeakReference(activity)
    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        @Nullable purchases: List<Purchase>?
    ) {

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        }
        else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED){
            onPurchased()
        }else purchaseNotSuccess()
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken).build()
                activityRef.get()?.billingClient?.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                    onPurchased()
                }
            }else{
                onPurchased()
            }
        }
    }
    private fun onPurchased(){

        StorageSharedPref.setAppPurchased(true)
        activityRef.get()?.billingClient?.endConnection()
        activityRef.get()?.startActivity(Intent(activityRef.get(), DownloadLinkActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
        activityRef.get()?.finish()
    }
    private fun purchaseNotSuccess(){
        StorageSharedPref.setAppPurchased(false)
    }

}