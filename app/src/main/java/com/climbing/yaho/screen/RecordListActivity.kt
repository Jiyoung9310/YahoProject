package com.climbing.yaho.screen

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityRecordListBinding
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.screen.ClimbingDetailActivity.Companion.startClimbingDetailActivity
import com.climbing.yaho.ui.RecordListAdapter
import com.climbing.yaho.viewmodel.RecordListViewModel
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    private val TAG = this::class.simpleName
    @Inject
    lateinit var yahoPreference: YahoPreference
    private val viewModel by viewModels<RecordListViewModel>()
    private lateinit var recordListAdapter : RecordListAdapter
    private var recordHeaderList : Array<String>? = null
    private var selectedDate : Int = 0
    private var mInterstitialAd: InterstitialAd? = null
    private val adRequest by lazy { AdRequest.Builder().build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this) { }
        initView()
        initObserve()
    }

    private fun initView() {
        loadAdmob(yahoPreference.isSubscribing)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        recordListAdapter = RecordListAdapter(
            clickItem = { id, name ->
                mInterstitialAd?.let {
                    showInterstitial {
                        viewModel.onClickRecordDetail(id, name)
                    }
                } ?: viewModel.onClickRecordDetail(id, name)
            },
            selectDate = {
                if(!recordHeaderList.isNullOrEmpty()) {
                    AlertDialog.Builder(this@RecordListActivity)
                        .setSingleChoiceItems(recordHeaderList, selectedDate) { dialog, which ->
                            viewModel.onSelectDate(which)
                            selectedDate = which
                            dialog.dismiss()
                        }.create().show()
                }
            })

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@RecordListActivity, RecyclerView.VERTICAL, false)
            adapter = recordListAdapter
        }
    }

    private fun initObserve() {
        viewModel.recordList.observe(this) {
            recordListAdapter.submitList(it)
        }
        viewModel.recordHeaderDateList.observe(this) { list ->
            recordHeaderList = list
        }
        viewModel.goToRecordDetail.observe(this) { (id, name) ->
            startClimbingDetailActivity(
                activity = this,
                climbingId = id,
                mountainName = name
            )
        }
    }

    private fun loadAdmob(isSubscribing: Boolean) {
        binding.adContainer.isVisible = !isSubscribing
        if(isSubscribing) return

        InterstitialAd.load(this, getString(R.string.admob_full_screen_unit_id), adRequest, object : InterstitialAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d(TAG, adError?.message)
                mInterstitialAd = null
            }

            override fun onAdLoaded(interstitialAd: InterstitialAd) {
                Log.d(TAG, "Ad was loaded.")
                mInterstitialAd = interstitialAd
            }
        })

        // 하단 배너 광고 세팅
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

        adView.apply {
            adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this@RecordListActivity, adWidth)
            adUnitId = getString(R.string.admob_banner_unit_id)
            // Start loading the ad in the background.
            loadAd(adRequest)
        }
    }

    private fun showInterstitial(afterAd: () -> Unit) {
        mInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                Log.d(TAG, "Ad was dismissed.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                mInterstitialAd = null
                afterAd.invoke()
                loadAdmob(yahoPreference.isSubscribing)
            }

            override fun onAdFailedToShowFullScreenContent(adError: AdError?) {
                Log.d(TAG, "Ad failed to show.")
                // Don't forget to set the ad reference to null so you
                // don't show the ad a second time.
                mInterstitialAd = null
                afterAd.invoke()
            }

            override fun onAdShowedFullScreenContent() {
                Log.d(TAG, "Ad showed fullscreen content.")
            }
        }
        mInterstitialAd?.show(this)
    }
}