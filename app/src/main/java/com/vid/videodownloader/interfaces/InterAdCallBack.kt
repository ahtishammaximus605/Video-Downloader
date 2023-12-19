package com.vid.videodownloader.interfaces


interface BannerCallback {
    fun onAdLoaded()
    fun onAdFailed()
}
interface InterAdLoadCallBack {
    fun onAdFailed()
    fun onAdLoaded()
}
interface InterAdCallBack {
    fun onAdClosed()
    fun onAdFailed()
    fun onAdOff()
}
interface NativeAdCallback {
     fun onAdReloadStarted()
     fun onAdLoaded()
     fun onSecondaryAdLoaded()
     fun onAdFailed()
}