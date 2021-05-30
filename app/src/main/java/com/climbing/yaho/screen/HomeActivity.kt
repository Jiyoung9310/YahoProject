package com.android.yaho.screen

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.SkuDetails
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.billing.BillingModule
import com.android.yaho.billing.Sku
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
    private lateinit var menuAdapter: HomeMenuAdapter
    private lateinit var bm: BillingModule
    private var skuDetails = listOf<SkuDetails>()
        set(value) {
            field = value
            setSkuDetailsView()
        }

    private var currentSubscription: Purchase? = null
        set(value) {
            field = value
            updateSubscriptionState()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initBilling()
        initView()
        initObserve()
        viewModel.getUserData()
    }

    override fun onResume() {
        super.onResume()
        bm.onResume(BillingClient.SkuType.SUBS)
    }

    private fun initBilling() {
        bm = BillingModule(this, lifecycleScope, object: BillingModule.Callback {
            override fun onBillingModulesIsReady() {
                bm.querySkuDetail(BillingClient.SkuType.SUBS, Sku.REMOVE_ADS) {
                    skuDetails = it
                }

                bm.checkSubscribed {
                    currentSubscription = it
                }
            }

            override fun onSuccess(purchase: Purchase) {
                currentSubscription = purchase
            }

            override fun onFailure(errorCode: Int) {
                when (errorCode) {
                    BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED -> {
                        Toast.makeText(applicationContext, "이미 구입한 상품입니다.", Toast.LENGTH_LONG).show()
                    }
                    BillingClient.BillingResponseCode.USER_CANCELED -> {
                        Toast.makeText(applicationContext, "구매를 취소하셨습니다.", Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        Toast.makeText(applicationContext, "error: $errorCode", Toast.LENGTH_LONG).show()
                    }
                }
            }

        })

    }

    private fun setSkuDetailsView() {
        val builder = StringBuilder()
        for (skuDetail in skuDetails) {
            builder.append("<${skuDetail.title}> : ")
            builder.append(skuDetail.price)
        }
        Toast.makeText(this, builder, Toast.LENGTH_SHORT).show()
    }

    private fun updateSubscriptionState() {
        currentSubscription?.let {
//            binding.tvSubscription.text = "구독중: ${it.sku} | 자동갱신: ${it.isAutoRenewing}"
            menuAdapter.menuList = mutableListOf(
                HomeMenuAdapter.VIEW_TYPE_START_CLIMBING,
                HomeMenuAdapter.VIEW_TYPE_MY_CLIMBS,
                HomeMenuAdapter.VIEW_TYPE_REMOVE_ADS_DONE
            )
        } ?: also {
//            binding.tvSubscription.text = "구독안함"
            menuAdapter.menuList = mutableListOf(
                HomeMenuAdapter.VIEW_TYPE_START_CLIMBING,
                HomeMenuAdapter.VIEW_TYPE_MY_CLIMBS,
                HomeMenuAdapter.VIEW_TYPE_REMOVE_ADS
            )
        }
    }

    private fun initView() {
        loadAdmob()
        menuAdapter = HomeMenuAdapter(
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
                skuDetails.find { it.sku == Sku.REMOVE_ADS }?.let { skuDetail ->
                    bm.purchase(skuDetail, currentSubscription)
                } ?: also {
                    Toast.makeText(applicationContext, "상품을 찾을 수 없습니다.", Toast.LENGTH_LONG).show()
                }
            }
        )

        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = menuAdapter
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