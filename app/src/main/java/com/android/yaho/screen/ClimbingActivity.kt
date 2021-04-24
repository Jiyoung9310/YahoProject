package com.android.yaho.screen

import android.os.Bundle
import android.os.PersistableBundle
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingBinding
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback

class ClimbingActivity : BindingActivity<ActivityClimbingBinding>(ActivityClimbingBinding::inflate),
    OnMapReadyCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
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
    }

    override fun onMapReady(naverMap: NaverMap) {

    }
}