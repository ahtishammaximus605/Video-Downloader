package com.vid.videodownloader.ads


import android.content.Context
import android.os.Bundle
import androidx.annotation.Size
import com.google.firebase.analytics.FirebaseAnalytics

class FirebaseAnalytic(context: Context)  {
    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    fun sendEventAnalytic(@Size(min = 1L, max = 40L) eventName: String, eventStatus: String) {
        val bundle = Bundle()
        bundle.putString(FirebaseAnalytics.Param.VALUE, eventStatus)
        firebaseAnalytics.logEvent(eventName, bundle)
    }

}