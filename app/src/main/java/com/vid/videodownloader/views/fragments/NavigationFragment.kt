package com.vid.videodownloader.views.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import com.vid.videodownloader.MyApp
import com.vid.videodownloader.R
import com.vid.videodownloader.databinding.FragmentNavigationBinding
import com.vid.videodownloader.services.DownloadService
import com.vid.videodownloader.utils.StorageSharedPref
import com.vid.videodownloader.views.*
import com.vid.videodownloader.views.SplashActivity.Companion.config
import com.vid.videodownloader.views.dialogs.PremiumDialog
import com.vid.videodownloader.views.dialogs.dialogInterface



class NavigationFragment : Fragment() {
    var type : Int = 1
    private lateinit var binding: FragmentNavigationBinding
    override fun onAttach(context: Context) {
        super.onAttach(context)
        arguments?.getInt("type")?.let {
            type = it
        }
    }
    override fun onResume() {
        super.onResume()

        setView(type)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNavigationBinding.inflate(inflater, container, false)
        //set variables in Binding
        return binding.root
    }
    fun setView(type : Int){
        when (type) {
            1 -> {
                select(binding.btnLink,binding.view)
            }
            2 -> {
                select(binding.btnfbBrowser,binding.view2)
            }
            3 -> {
                select(binding.btnDownloads,binding.view3)
            }
            4 -> {
                select(binding.btnVideos,binding.view4)
            }
            5 -> {
                select(binding.imgMore
                    ,binding.view5)
            }
        }
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setView(type)
        binding.imgGift.setOnClickListener {

            //startActivity(Intent(context, PremiumActivity::class.java))
            val dialog = activity?.let { it1 -> PremiumDialog(it1) }
            dialog?.dg = object : dialogInterface {
                override fun onClose() {
                    if(activity?.application is MyApp)
                    (activity?.application as MyApp).purchase(activity!!)
                }
            }
            dialog?.window?.setBackgroundDrawable(
                ColorDrawable(
                    Color.TRANSPARENT
                )
            )
            dialog?.setCanceledOnTouchOutside(true)
            dialog?.show()
            val window: Window? = dialog?.window
            window?.setLayout(
                ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )

        }
        if(config.dashboard_Activity_on_off.value=="on")
        {
            binding.homeBtnn.visibility=View.VISIBLE
        }
        else{
            binding.homeBtnn.visibility=View.GONE
        }
        binding.imMenu.setOnClickListener {
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.more_app))
                    )
                )
            } catch (e: Exception) {
            }
        }
        binding.homeBtnn.setOnClickListener {
            if(config.dashboard_Activity_on_off.value=="on")
            {
               requireActivity().finish()

            }


        }
        binding.btnLink.setOnClickListener {
            select(it as ImageView,binding.view)
            startActivity(Intent(activity, DownloadLinkActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }
        binding.btnfbBrowser.setOnClickListener {
          //  select(it as ImageView,binding.view2)
           // startActivity(Intent(activity, FbBrowserActivity::class.java))

        }
        binding.btnDownloads.setOnClickListener {
            DownloadService.vidCount = 0
            DownloadService.videoDownloadStartedLiveData.postValue("0")
            select(it as ImageView,binding.view3)
            startActivity(Intent(activity, DownloadsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)

       }
        if(StorageSharedPref.isAppPurchased() == true) {
            binding.imgGift.visibility = View.GONE
        }
        binding.btnVideos.setOnClickListener {
            select(it as ImageView,binding.view4)
            startActivity(Intent(activity, CastActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }
        binding.imgMore.setOnClickListener {
            select(it as ImageView,binding.view5)
            startActivity(Intent(activity, SettingsActivity::class.java))
            requireActivity().finish()
            requireActivity().overridePendingTransition(0, 0)
        }


        val liveData: LiveData<String> = DownloadService.videoDownloadedLiveData
        val liveDataStarted: LiveData<String> = DownloadService.videoDownloadStartedLiveData
        liveData.observe(viewLifecycleOwner) {
            if (it == "1") {
                DownloadService.videoDownloadedLiveData.postValue("0")
                Toast.makeText(context, "Download Completed!", Toast.LENGTH_LONG).show()
            }
        }
        liveDataStarted.observe(viewLifecycleOwner) {
            if (it == "1") {
                binding.txtCount.visibility = View.VISIBLE
                val count = DownloadService.vidCount
                if (count > 0)
                    binding.txtCount.text = "" + count
                else binding.txtCount.visibility = View.INVISIBLE
            } else binding.txtCount.visibility = View.INVISIBLE

        }
    }
    var nextIntent :Intent? = null
    val startForResult2 = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            if(activity != null && activity?.window != null) {
                startActivity(nextIntent)
                //activity?.finish()
            }

        }
    }

    fun select(view1 : ImageView,view : View){
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.gray) }?.let { it2 ->
            binding.btnLink.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.gray) }?.let { it2 ->
            binding.btnfbBrowser.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.gray) }?.let { it2 ->
            binding.btnDownloads.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.gray) }?.let { it2 ->
            binding.btnVideos.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.gray) }?.let { it2 ->
            binding.imgMore.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
        binding.view.visibility = View.INVISIBLE
        binding.view2.visibility = View.INVISIBLE
        binding.view3.visibility = View.INVISIBLE
        binding.view4.visibility = View.INVISIBLE
        binding.view5.visibility = View.INVISIBLE

        view.visibility = View.VISIBLE
        context?.let { it1 -> ContextCompat.getColor(it1, R.color.purple_500) }?.let { it2 ->

            view1.setColorFilter(
                it2, android.graphics.PorterDuff.Mode.SRC_IN)
        }
    }

    companion object {
        fun newInstance(type : Int) : NavigationFragment{
            val fragment = NavigationFragment()
            fragment.arguments = Bundle().apply {
                putInt("type", type)
            }
            return fragment
        }
    }
}