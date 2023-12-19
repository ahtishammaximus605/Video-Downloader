package com.vid.videodownloader.views.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.FeedbackDialogBinding

class FeedbackDialog(private var _activity: Activity) : Dialog(_activity) {
    lateinit var binding: FeedbackDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = FeedbackDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.editFeedbck.onFocusChangeListener = View.OnFocusChangeListener { _, _ ->
            binding.editFeedbck.post(Runnable {
                val inputMethodManager: InputMethodManager =
                    _activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.showSoftInput(
                    binding.editFeedbck,
                    InputMethodManager.SHOW_IMPLICIT
                )
            })
        }
        binding.editFeedbck.requestFocus()
        binding.btnclose.setOnClickListener {
            dismiss()
        }
        binding.btnsave.setOnClickListener {
            val textFeed = binding.editFeedbck.text?.toString()

            if (binding.editFeedbck.text.toString().isEmpty()) {
                binding.editFeedbck.error = "No feedback is provided!"
                binding.editFeedbck.requestFocus()
            } else {
                try {
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_SUBJECT, _activity.resources.getString(R.string.app_name))
                    intent.putExtra(
                        Intent.EXTRA_EMAIL,
                        arrayOf<String>(_activity.resources.getString(R.string.email))
                    )
                    intent.putExtra(Intent.EXTRA_TEXT, textFeed)
                    intent.type = "message/rfc822"
                    intent.setPackage("com.google.android.gm")
                    _activity.startActivityForResult(intent, 256)
                }
                catch (ex: ActivityNotFoundException){
                    val intent = Intent(Intent.ACTION_SEND)
                    intent.putExtra(Intent.EXTRA_SUBJECT, _activity.resources.getString(R.string.app_name))
                    intent.putExtra(
                        Intent.EXTRA_EMAIL,
                        arrayOf<String>(_activity.resources.getString(R.string.email))
                    )
                    intent.putExtra(Intent.EXTRA_TEXT, textFeed)
                    intent.type = "message/rfc822"
                    _activity.startActivityForResult(intent, 256)
                }
                dismiss()

            }


        }
    }
}