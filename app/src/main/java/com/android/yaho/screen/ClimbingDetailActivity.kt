package com.android.yaho.screen

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDetailBinding
import com.android.yaho.millisecondsToHourTimeFormat
import com.android.yaho.ui.ClimbingDetailSectionAdapter
import com.android.yaho.viewmodel.ClimbingDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.extras?.getString(KEY_CLIMBING_DATA_ID)?.let {
            viewModel.getClimbingData(it)
        } ?: run {
            finish()
        }

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

        viewModel.sectionData.observe(this) {
            climbingDetailSectionAdapter.sectionList = it
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "데이터를 불러올 수 없습니다. ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }
}