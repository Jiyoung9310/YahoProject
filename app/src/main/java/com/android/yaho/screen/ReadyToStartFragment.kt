package com.android.yaho.screen

import android.os.Bundle
import android.view.View
import com.android.yaho.R
import com.android.yaho.base.BindingFragment
import com.android.yaho.data.MountainData
import com.android.yaho.databinding.FragmentReadyToStartBinding
import com.android.yaho.screen.ReadyActivity.Companion.KEY_SELECT_MOUNTAIN
import com.android.yaho.screen.ReadyActivity.Companion.SCREEN_NEAR_MOUNTAIN
import com.android.yaho.viewmodel.ReadyViewModel
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ReadyToStartFragment: BindingFragment<FragmentReadyToStartBinding>(FragmentReadyToStartBinding::inflate),
    OnMapReadyCallback {

    private val viewModel: ReadyViewModel by sharedViewModel()
    private val mountainData : MountainData? by lazy {
        arguments?.getParcelable(KEY_SELECT_MOUNTAIN)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(mountainData == null) viewModel.moveScreen(SCREEN_NEAR_MOUNTAIN)
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        initView()
    }

    private fun initView() {
        binding.tvNearTitle.text = getString(R.string.ready_to_start_title, mountainData?.name)
    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.apply {
            isLiteModeEnabled = true
            setBackgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
            mapType = NaverMap.MapType.Terrain
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)
            minZoom = 4.0
        }

        val location = viewModel.currentLocation
        location?.let {
            val cameraUpdate = CameraUpdate.scrollTo(LatLng(it.latitude, it.longitude))
                .animate(CameraAnimation.Easing)
            naverMap.moveCamera(cameraUpdate)
        }

    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}