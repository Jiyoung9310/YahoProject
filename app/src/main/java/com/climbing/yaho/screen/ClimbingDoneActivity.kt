package com.climbing.yaho.screen

import android.animation.Animator
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.climbing.yaho.BuildConfig
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityClimbingDoneBinding
import com.climbing.yaho.ui.ClimbingSaveDialog
import com.climbing.yaho.viewmodel.ClimbingDoneViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDoneActivity : BindingActivity<ActivityClimbingDoneBinding>(
    ActivityClimbingDoneBinding::inflate
) {
    private val TAG = this::class.java.simpleName

    private val viewModel by viewModel<ClimbingDoneViewModel>()
    private lateinit var connectivityManager : ConnectivityManager
    private var dialog : ClimbingSaveDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        dialog = ClimbingSaveDialog(this)
        dialog?.show()

        initView()
        initObserve()
    }
    private fun initView() {
        loadAdmob()
        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.e(TAG, "The default network is now: " + network)
                viewModel.saveClimbData()
                dialog?.dismiss()
                binding.lottieDone.playAnimation()
            }
        })

        binding.lottieDone.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

            }

            override fun onAnimationEnd(p0: Animator?) {
                viewModel.animationEnd()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

    private fun loadAdmob() {
        MobileAds.initialize(this) { }

        val adView = AdView(this)
        binding.adContainer.addView(adView)

        val display = windowManager.defaultDisplay
        val outMetrics = DisplayMetrics()
        display.getMetrics(outMetrics)

        val density = outMetrics.density

        var adWidthPixels = binding.adContainer.width.toFloat()
        if (adWidthPixels == 0f) {
            adWidthPixels = outMetrics.widthPixels.toFloat()
        }

        val adWidth = (adWidthPixels / density).toInt()

        adView.adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, adWidth)
        adView.adUnitId = getString(if(BuildConfig.DEBUG) R.string.admob_banner_unit_id_test else R.string.admob_banner_unit_id)


        // Create an ad request.
        val adRequest = AdRequest.Builder().build()

        // Start loading the ad in the background.
        adView.loadAd(adRequest)
    }

    private fun initObserve() {
        viewModel.saveResult.observe(this) {
            Toast.makeText(this, getString(R.string.climbing_save_done), Toast.LENGTH_LONG).show()
        }

        viewModel.goToDetail.observe(this) {
            ClimbingDetailActivity.startClimbingDetailActivity(this, it)
            finish()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "error : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}