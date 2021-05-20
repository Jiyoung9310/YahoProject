package com.android.yaho.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDetailBinding
import com.android.yaho.dp
import com.android.yaho.millisecondsToHourTimeFormat
import com.android.yaho.ui.ClimbingDetailSectionAdapter
import com.android.yaho.viewmodel.ClimbingDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
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

        fun startClimbingDetailActivity(
            activity: Activity,
            climbingId: String,
        ) {
            activity.startActivity(Intent(activity, ClimbingDetailActivity::class.java).apply {
                putExtra(KEY_CLIMBING_DATA_ID, climbingId)
            })
        }
    }

    private val viewModel by viewModel<ClimbingDetailViewModel>()
    private var naverMap : NaverMap? = null
    private lateinit var climbingDetailSectionAdapter : ClimbingDetailSectionAdapter
    private val pathOverlay: PathOverlay by lazy { PathOverlay() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        binding.btnClose.setOnClickListener {
            finish()
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
            naverMap?.let {
                list.forEach {
                    Marker().apply {
                        position = LatLng(it.latitude, it.longitude)
                        icon = OverlayImage.fromResource(R.drawable.img_marker_section)
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
            it.outlineWidth = 0
            it.color = Color.BLACK
        }

        intent.extras?.getString(KEY_CLIMBING_DATA_ID)?.let {
            viewModel.getClimbingData(it)
        } ?: run {
            finish()
        }
    }
}