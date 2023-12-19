package com.vid.videodownloader.views.dialogs

import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.vid.videodownloader.databinding.RateDialogBinding
import me.zhanghai.android.materialratingbar.MaterialRatingBar

class RateDialog (private var _activity: Activity) : Dialog(_activity) {
    lateinit var binding: RateDialogBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = RateDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.close.setOnClickListener {
            dismiss()
        }
        binding.ratingBar.onRatingChangeListener =
            MaterialRatingBar.OnRatingChangeListener { ratingBar, rating ->
                val rC = binding.ratingBar.rating
                when {
                    rC == 0F -> {
                        Toast.makeText(_activity, "Please rate.", Toast.LENGTH_LONG).show()
                    }
                    rC > 4 && rC > 0.0 -> {

                        val uri = Uri.parse("market://details?id=" + _activity.packageName)
                        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                        try {
                            _activity.startActivity(myAppLinkToMarket)
                        } catch (e: ActivityNotFoundException) {
                            Toast.makeText(_activity, "Couldn't find app.", Toast.LENGTH_LONG).show()
                        }
                        dismiss()
                    }
                    else -> {
                        showThankDialog()
                        dismiss()
                    }
                }
            }
    }
    private fun showThankDialog() {
        val b = AlertDialog.Builder(_activity)
        b.setMessage("Thank-You for rating!")
        b.setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
        b.create().show()
    }
}