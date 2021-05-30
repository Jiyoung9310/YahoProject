package com.android.yaho.screen

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityHomeBinding
import com.android.yaho.dp
import com.android.yaho.meter
import com.android.yaho.ui.HomeMenuAdapter
import com.android.yaho.viewmodel.HomeViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeActivity : BindingActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private val viewModel by viewModel<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        viewModel.getUserData()
    }

    private fun initView() {
        loadAdmob()
        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = HomeMenuAdapter(
                startClimbingClickAction = {
                    // 등산 기록하기 화면으로 이동
                    startActivity(Intent(this@HomeActivity, ReadyActivity::class.java))
                },
                myClimbsClickAction = {
                    // 등산 기록 확인하기 화면으로 이동
//                    startClimbingDetailActivity(this@HomeActivity, "1621395171715")
                    startActivity(Intent(this@HomeActivity, RecordListActivity::class.java))
                },
                removeAdsClickAction = {
                    // 광고 제거 결제 화면으로 이동 
                }
            )
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.left = 12.dp
                    outRect.right = 12.dp

                    view.layoutParams.width = (parent.width * 0.8).toInt()
                }
            })
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)

                    val offset = 20.dp
                    val itemCount = state.itemCount
                    val childPosition = parent.getChildAdapterPosition(view)
                    if(childPosition == 0) {
                        outRect.left = offset
                    } else if (childPosition == itemCount - 1) {
                        outRect.right = offset
                    }
                }
            })

            if (onFlingListener == null) PagerSnapHelper().attachToRecyclerView(this)
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

    private fun initObserve() {
        viewModel.userData.observe(this) {
            binding.tvAllHeight.text = it.allHeight.meter(this)
            binding.tvClimbNumber.text = getString(R.string.count_unit, it.totalCount)
        }
    }
}