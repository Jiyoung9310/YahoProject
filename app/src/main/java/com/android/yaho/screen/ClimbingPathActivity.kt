package com.android.yaho.screen

import android.os.Bundle
import android.util.Log
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingPathBinding
import com.android.yaho.dp
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback

class ClimbingPathActivity : BindingActivity<ActivityClimbingPathBinding>(ActivityClimbingPathBinding::inflate),
    OnMapReadyCallback {

    private var naverMap: NaverMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        initView()
    }

    private fun initView() {
        binding.btnClose.setOnClickListener {
            finish()
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
            setContentPadding(0, 0, 0, 300.dp)
        }
    }
}