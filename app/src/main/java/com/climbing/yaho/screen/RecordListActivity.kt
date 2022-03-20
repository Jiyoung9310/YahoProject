package com.climbing.yaho.screen

import android.os.Bundle
import android.util.DisplayMetrics
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.climbing.yaho.BuildConfig
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityRecordListBinding
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.screen.ClimbingDetailActivity.Companion.startClimbingDetailActivity
import com.climbing.yaho.ui.RecordListAdapter
import com.climbing.yaho.viewmodel.RecordListViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    @Inject
    lateinit var yahoPreference: YahoPreference
    private val viewModel by viewModels<RecordListViewModel>()
    private lateinit var recordListAdapter : RecordListAdapter
    private var recordHeaderList : Array<String>? = null
    private var selectedDate : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                startClimbingDetailActivity(
                    activity = this,
                    climbingId = id,
                    mountainName = name
                )
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
    }

    private fun loadAdmob(isSubscribing: Boolean) {
        binding.adContainer.isVisible = !isSubscribing
        if(isSubscribing) return

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
}