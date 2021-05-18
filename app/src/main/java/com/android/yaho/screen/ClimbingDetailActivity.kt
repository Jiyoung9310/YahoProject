package com.android.yaho.screen

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDetailBinding
import com.android.yaho.millisecondsToHourTimeFormat
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
    }

    private val viewModel by viewModel<ClimbingDetailViewModel>()
    private val behavior by lazy { BottomSheetBehavior.from(binding.clDetailInfo) }
    private var naverMap : NaverMap? = null

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


        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slide

            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // state changed
            }
        })
    }

    private fun initObserve() {
        viewModel.climbingData.observe(this) { record ->
            record?.let {
                binding.tvDate.text = it.climbingDate
                binding.tvMountainName.text = getString(R.string.climbing_detail_mountain_name, it.mountainName, it.mountainVisitCount)
                binding.tvTotalTime.text = it.allRunningTime.millisecondsToHourTimeFormat()
                binding.tvClimbingTime.text = it.totalClimbingTime.millisecondsToHourTimeFormat()
                binding.tvRestTime.text = (it.allRunningTime - it.totalClimbingTime).millisecondsToHourTimeFormat()
                binding.tvDistance.text = getString(R.string.kilo_meter_unit, it.totalDistance)
                binding.tvAverageSpeed.text = getString(R.string.speed_unit, it.averageSpeed.toFloat())
                binding.tvMaxSpeed.text = getString(R.string.speed_unit, it.maxSpeed)
                binding.tvStartHeight.text = getString(R.string.meter_unit, it.startHeight)
                binding.tvMaxHeight.text = getString(R.string.meter_unit, it.maxHeight)
            } ?: run {
                Log.d(TAG, "데이터가 null")
            }
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "데이터를 불러올 수 없습니다. ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
    }
}