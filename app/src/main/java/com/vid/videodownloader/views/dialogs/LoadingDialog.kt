package com.vid.videodownloader.views.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.os.Looper
import android.view.Window
import android.view.WindowManager
import com.vid.videodownloader.R

class LoadingDialog(context: Context) : Dialog(context) {

    init {

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_loading)
        window?.setBackgroundDrawable(ColorDrawable(Color.parseColor("#77000000")))
        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(this.window?.attributes)
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT
        this.window?.attributes = layoutParams
        setCancelable(false)
        try {
            Handler(Looper.getMainLooper()).postDelayed({ this.dismiss() }, 12000)
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }
}
