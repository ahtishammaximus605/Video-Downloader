package com.vid.videodownloader.views

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ShareCompat

import com.vid.videodownloader.databinding.ActivityShareBinding


class ShareActivity : BaseActivity<ActivityShareBinding>({ ActivityShareBinding.inflate(it) }) {
    var uri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        adContent = binding.adContent
        val url = intent.extras?.getString("url")
        if (url != null) {
            uri =
                Uri.parse(url)
        }

        binding.textView31.setOnClickListener {
            onBackPressed()
        }

        binding.btnSms.setOnClickListener {
            isAdClicked = true
            try {
                if (uri != null) {
                    val sharingIntent = Intent(Intent.ACTION_SEND);
                    sharingIntent.type = "video/*"; //If it is a 3gp video use ("video/3gp")
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sharingIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Download this app:  http://play.google.com/store/apps/details?id=$packageName"
                    );
                    startActivity(Intent.createChooser(sharingIntent, "Download Video!"));
                } else
                    ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setChooserTitle("Share App")
                        .setText("http://play.google.com/store/apps/details?id=$packageName")
                        .startChooser()
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "No app to open.", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnShare.setOnClickListener {
            isAdClicked = true
            try {
                if (uri != null) {

                    val sharingIntent = Intent(Intent.ACTION_SEND);
                    sharingIntent.type = "video/*"; //If it is a 3gp video use ("video/3gp")
                    sharingIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sharingIntent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Download this app:  http://play.google.com/store/apps/details?id=$packageName"
                    );
                    startActivity(Intent.createChooser(sharingIntent, "Download Video!"));
//                    ShareCompat.IntentBuilder.from(this)
//                        .setType("video/*")
//                        .setChooserTitle("Shared Video!")
//                    .setText(
//                        "Download this app:  http://play.google.com/store/apps/details?id=$packageName"
//                    )
//                        .setStream(uri)
//                        .startChooser()
                } else
                    ShareCompat.IntentBuilder.from(this)
                        .setType("text/plain")
                        .setChooserTitle("Share App")
                        .setText("http://play.google.com/store/apps/details?id=$packageName")
                        .startChooser()
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "No app to open.", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnGmail.setOnClickListener {
            try {
                isAdClicked = true
                val intent = Intent(Intent.ACTION_SEND)

                intent.type = "message/rfc822"

                if (uri != null) {
                    intent.putExtra(Intent.EXTRA_TITLE, "Shared Video!")
                    intent.type = "video/*"
                    intent.putExtra(Intent.EXTRA_STREAM, uri)
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "Download this app:  http://play.google.com/store/apps/details?id=$packageName"
                    )

                } else {
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Shared App")
                    intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "http://play.google.com/store/apps/details?id=$packageName"
                    )
                }


                intent.setPackage("com.google.android.gm")
                startActivity(intent)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "No app to open.", Toast.LENGTH_LONG).show()
            }
        }
        binding.btnTwitter.setOnClickListener {
            try {
                isAdClicked = true
                val share = Intent(Intent.ACTION_SEND)

                share.setPackage("com.twitter.android")
                if (uri != null) {
                    share.putExtra(Intent.EXTRA_TITLE, "Shared Video!")
                    share.putExtra(
                        Intent.EXTRA_TEXT,
                        "Download this app:  http://play.google.com/store/apps/details?id=$packageName"
                    )
                    share.type = "video/*"
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                } else {
                    val settype = "text/plain"
                    share.type = settype
                    share.putExtra(
                        Intent.EXTRA_TEXT,
                        "http://play.google.com/store/apps/details?id=$packageName"
                    )
                }
                startActivity(Intent.createChooser(share, "Share to"))
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "No app to open.", Toast.LENGTH_LONG).show()
            }

        }
    }

    override val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                //if(isFinish)
                finish()
            }
        }

    override fun onBackPressed() {

        finish()
    }

    override fun onResume() {
        super.onResume()

    }

}