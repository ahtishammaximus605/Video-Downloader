package com.vid.videodownloader.views.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import com.android.billingclient.api.*
import com.vid.videodownloader.databinding.PremiumDialogBinding
import com.vid.videodownloader.interfaces.InterAdCallBack
import com.vid.videodownloader.model.RemoteKeys

interface dialogInterface{
    fun onClose()
}
class PremiumDialog(private var _activity: Activity) : Dialog(_activity)
{
    var dg : dialogInterface? = null

    lateinit var binding: PremiumDialogBinding
    override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
        super.setOnDismissListener(listener)
        dg = null
    }
    private var isBack = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = PremiumDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(false)

        binding.textView25.setOnClickListener {
                        dismiss()
        }
        binding.lytPurchase2.setOnClickListener {
            dg?.onClose()
            dg = null
            dismiss()
        }
        binding.textView25.visibility = View.INVISIBLE
        Handler(Looper.myLooper()!!).postDelayed({
            binding.textView25.visibility = View.VISIBLE
            isBack = true
            setCanceledOnTouchOutside(true)
        }, 3000)
    }
}