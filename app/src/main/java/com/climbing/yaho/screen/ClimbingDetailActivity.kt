package com.climbing.yaho.screen

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.PointF
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.climbing.yaho.BuildConfig
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityClimbingDetailBinding
import com.climbing.yaho.screen.ClimbingPathActivity.Companion.KEY_PATH_LIST
import com.climbing.yaho.screen.ClimbingPathActivity.Companion.KEY_SECTION_MARK_LIST
import com.climbing.yaho.ui.ClimbingDetailSectionAdapter
import com.climbing.yaho.viewmodel.ClimbingDetailViewModel
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDetailActivity : BindingActivity<ActivityClimbingDetailBinding>(ActivityClimbingDetailBinding::inflate),
    OnMapReadyCallback {
    private val TAG = this::class.java.simpleName
    companion object {
        const val KEY_CLIMBING_DATA_ID = "KEY_CLIMBING_DATA_ID"
        const val KEY_SHOW_MOUNTAIN_NAME = "KEY_SHOW_MOUNTAIN_NAME"

        fun startClimbingDetailActivity(
            activity: Activity,
            climbingId: String,
            mountainName: String? = null
        ) {
            activity.startActivity(Intent(activity, ClimbingDetailActivity::class.java).apply {
                putExtra(KEY_CLIMBING_DATA_ID, climbingId)
                mountainName?.let { putExtra(KEY_SHOW_MOUNTAIN_NAME, it) }
            })
        }
    }

    private val viewModel by viewModel<ClimbingDetailViewModel>()
    private var naverMap : NaverMap? = null
    private lateinit var climbingDetailSectionAdapter : ClimbingDetailSectionAdapter
    private val pathOverlay: PathOverlay by lazy { PathOverlay() }

    private val pathList = arrayListOf<com.climbing.yaho.local.db.LatLng>()
    private val sectionMarkList = arrayListOf<com.climbing.yaho.local.db.LatLng>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        loadAdmob()
        intent.hasExtra(KEY_SHOW_MOUNTAIN_NAME).let {
            binding.toolbarTitle.isVisible = it
            binding.btnDelete.isVisible = it
            binding.btnClose.isVisible = !it
            if(it) binding.toolbar.setNavigationIcon(R.drawable.ic_back)
        }

        intent.extras?.getString(KEY_SHOW_MOUNTAIN_NAME)?.let {
            binding.toolbarTitle.text = getString(R.string.climbing_detail_toolbar_title, it)
        }

        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnClose.setOnClickListener {
            onBackPressed()
        }

        binding.btnDelete.setOnClickListener {
            viewModel.deleteRecord()
        }

        binding.btnWide.setOnClickListener {
            startActivity(Intent(this, ClimbingPathActivity::class.java).apply {
                putExtra(KEY_PATH_LIST, pathList)
                putExtra(KEY_SECTION_MARK_LIST, sectionMarkList)
            })
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .backgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
                    .mapType(NaverMap.MapType.Terrain)
                    .enabledLayerGroups(NaverMap.LAYER_GROUP_MOUNTAIN)
                    .minZoom(4.0)
                    .maxZoom(15.0)
            ).also {
                supportFragmentManager.beginTransaction().add(R.id.mapFragment, it).commit()
            }

        mapFragment.getMapAsync(this)

        climbingDetailSectionAdapter = ClimbingDetailSectionAdapter()
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@ClimbingDetailActivity, RecyclerView.VERTICAL, false)
            adapter = climbingDetailSectionAdapter
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
        viewModel.noData.observe(this) {
            Log.d(TAG, "데이터가 null")
            Toast.makeText(this, "표시할 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        viewModel.climbingData.observe(this) {
            binding.tvDate.text = it.climbingDate
            binding.tvMountainName.text = it.mountainNameTitle
            binding.tvAddress.text = it.mountainAddress
            binding.tvTotalTime.text = it.allRunningTime
            binding.tvClimbingTime.text = it.totalClimbingTime
            binding.tvRestTime.text = it.restTime
            binding.tvDistance.text = it.totalDistance
            binding.tvAverageSpeed.text = it.averageSpeed
            binding.tvMaxSpeed.text = it.maxSpeed
            binding.tvStartHeight.text = it.startHeight
            binding.tvMaxHeight.text = it.maxHeight
        }

        viewModel.pathData.observe(this) { list ->
            pathList.addAll(list.map{
                com.climbing.yaho.local.db.LatLng(it.latitude, it.longitude)
            })
            naverMap?.let {
                if (list.size >= 2) {
                    pathOverlay.apply {
                        coords = list
                        map = naverMap
                    }
                }
            }
        }

        viewModel.sectionMark.observe(this) { list ->
            sectionMarkList.addAll(list.map{
                com.climbing.yaho.local.db.LatLng(it.latitude, it.longitude)
            })
            naverMap?.let {
                list.forEach {
                    Marker().apply {
                        anchor = PointF(0.1f, 0.7f)
                        position = LatLng(it.latitude, it.longitude)
                        icon = OverlayImage.fromResource(R.drawable.img_marker_dot)
                        isForceShowIcon = true
                        map = naverMap
                    }
                }

                val cameraUpdate = CameraUpdate.fitBounds(
                    LatLngBounds.Builder().include(list).build(), 150
                )
                naverMap?.moveCamera(cameraUpdate)
            }
        }

        viewModel.sectionData.observe(this) {
            climbingDetailSectionAdapter.sectionList = it
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "데이터를 불러올 수 없습니다. ${it.message}", Toast.LENGTH_SHORT).show()
        }

        viewModel.deleteDone.observe(this) {
            if(it) {
                Toast.makeText(applicationContext, getString(R.string.climbing_record_delete_done), Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        naverMap.apply {
            isLiteModeEnabled = true
            setBackgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
            mapType = NaverMap.MapType.Terrain
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)
            minZoom = 4.0
            maxZoom = 13.0
            uiSettings.apply {
                isZoomControlEnabled = false
                isCompassEnabled = false
                isScaleBarEnabled = false
            }
            setContentPadding(0, 0, 0, 0)
        }

        pathOverlay.also {
            it.width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
            it.outlineWidth = resources.getDimensionPixelSize(R.dimen.path_overlay_outline_width)
            it.color = Color.BLACK
        }

        intent.extras?.getString(KEY_CLIMBING_DATA_ID)?.let {
            viewModel.getClimbingData(it)
        } ?: run {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        startActivity(Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        })
        finish()
    }
}