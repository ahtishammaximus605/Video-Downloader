package com.vid.videodownloader.views.dialogs

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.constraintlayout.widget.ConstraintLayout

import com.vid.videodownloader.databinding.ExitDialogBinding
import kotlin.system.exitProcess


class ExitDialog(private var _activity: Activity) : Dialog(_activity) {
    lateinit var binding: ExitDialogBinding
    var adLyt : ConstraintLayout? = null
    var adProgressBar: ProgressBar? = null
    var adContent: FrameLayout? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ExitDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adLyt= binding.adLyt
        adProgressBar= binding.adProgressBar
        adContent= binding.adContent

        binding.btnSave.setOnClickListener {
           _activity.finishAndRemoveTask()
           _activity.finishAffinity()
           _activity.finish()
            exitProcess(0)
       }
        binding.btnCanc.setOnClickListener {
            dismiss()
        }

    }


}