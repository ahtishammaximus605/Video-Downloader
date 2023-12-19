package com.vid.videodownloader

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.vid.videodownloader.databinding.ActivityPrivacyPolicyBinding

class PrivacyPolicyActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPrivacyPolicyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPrivacyPolicyBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.webview.loadUrl("https://sites.google.com/view/statusvideodownloderpp/home")
    }
}