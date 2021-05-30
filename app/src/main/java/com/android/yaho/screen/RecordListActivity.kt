package com.android.yaho.screen

import android.content.DialogInterface
import android.graphics.Canvas
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AlertDialogLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityRecordListBinding
import com.android.yaho.dp
import com.android.yaho.screen.ClimbingDetailActivity.Companion.startClimbingDetailActivity
import com.android.yaho.ui.RecordListAdapter
import com.android.yaho.ui.SimpleDividerItemDecoration
import com.android.yaho.viewmodel.RecordListViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    private val viewModel by viewModel<RecordListViewModel>()
    private lateinit var recordListAdapter : RecordListAdapter
    private var recordHeaderList : Array<String>? = null
    private var selectedDate : Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        loadAdmob()
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
}