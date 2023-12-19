package com.vid.videodownloader.utils
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.vid.videodownloader.BuildConfig

class StorageSharedPref {
    companion object{
        var sp : SharedPreferences? = null
        private var storageEditor: SharedPreferences.Editor? = null
        fun verifyInstallerId(context: Context): Boolean {

            val validInstallers: List<String> = ArrayList(listOf("com.android.vending", "com.google.android.feedback"))
            val installer = context.packageManager.getInstallerPackageName(context.packageName)

            // true Play Store
            return if(BuildConfig.DEBUG){
                true
            } else
                installer != null && validInstallers.contains(installer)
        }
        fun isNetworkAvailable(context: Context): Boolean {
            return try {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                activeNetworkInfo != null && activeNetworkInfo.isConnected
            } catch (ex: Exception) {
                true
            }
        }
        fun setStorage(ctx: Context)
        {
            sp = ctx.getSharedPreferences(
                ctx.packageName + "_storage_video_downloader", Context.MODE_PRIVATE
            )
        }
        fun get(v: String?): String? {
            return sp?.getString(v, "")
        }
        fun saveRemote(key: String?, remoteConfig: FirebaseRemoteConfig?) {
            val value = key?.let { remoteConfig?.getString(it) }
            storageEditor = sp?.edit()
            storageEditor?.putString(key, value)
            storageEditor?.apply()
            storageEditor?.commit()
        }
        fun save(key: String?, value: String?) {
            storageEditor = sp?.edit()
            storageEditor?.putString(key, value)
            storageEditor?.apply()
            storageEditor?.commit()
        }
        fun save(key: String?, value: Int) {
            storageEditor = sp?.edit()
            storageEditor?.putInt(key, value)
            storageEditor?.apply()
            storageEditor?.commit()
        }
        fun getIntPref(v: String?): Int? {
            return sp?.getInt(v, 0)
        }
        fun isAppPurchased(): Boolean? {
            return sp?.getBoolean("video_app_purchased", false)
        }

        fun setAppPurchased(p: Boolean) {
            storageEditor = sp?.edit()
            storageEditor?.putBoolean("video_app_purchased", p)
            storageEditor?.apply()
            storageEditor?.commit()
        }

    }
}