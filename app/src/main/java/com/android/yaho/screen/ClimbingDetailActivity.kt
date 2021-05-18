package com.android.yaho.screen

import android.os.Bundle
import android.view.View
import androidx.core.view.isInvisible
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDetailBinding
import com.android.yaho.viewmodel.ClimbingDetailViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDetailActivity : BindingActivity<ActivityClimbingDetailBinding>(ActivityClimbingDetailBinding::inflate),
    OnMapReadyCallback {

    companion object {
        const val KEY_CLIMBING_DATA_ID = "KEY_CLIMBING_DATA_ID"
    }

    private val viewModel by viewModel<ClimbingDetailViewModel>()
    private val behavior by lazy { BottomSheetBehavior.from(binding.clDetailInfo) }

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

    }

    override fun onMapReady(naverMap: NaverMap) {

    }
}