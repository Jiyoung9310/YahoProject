package com.android.yaho.screen

import android.graphics.PointF
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
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.OverlayImage
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class ReadyToStartFragment: BindingFragment<FragmentReadyToStartBinding>(FragmentReadyToStartBinding::inflate),
    OnMapReadyCallback {

    private val viewModel: ReadyViewModel by sharedViewModel()
    private lateinit var mountainData : MountainData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getParcelable<MountainData>(KEY_SELECT_MOUNTAIN)?.let{
            mountainData = it
        } ?: run { viewModel.moveScreen(SCREEN_NEAR_MOUNTAIN) }

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)
        initView()
    }

    private fun initView() {
        binding.tvNearTitle.text = getString(R.string.ready_to_start_title, mountainData.name)
    }

    override fun onMapReady(naverMap: NaverMap) {
        naverMap.apply {
            isLiteModeEnabled = true
            setBackgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
            mapType = NaverMap.MapType.Terrain
            setLayerGroupEnabled(NaverMap.LAYER_GROUP_MOUNTAIN, true)
            minZoom = 4.0
            maxZoom = 13.0
            setContentPadding(100, 100, 100, 100)
            uiSettings.apply {
                isTiltGesturesEnabled = false
                isRotateGesturesEnabled = false
                isScrollGesturesEnabled = false
                isZoomGesturesEnabled = false
                isZoomControlEnabled = false
                isCompassEnabled = false
                isScaleBarEnabled = false
            }
        }

        val location = viewModel.currentLocation
        location?.let {
            val cameraUpdate = CameraUpdate.fitBounds(
                LatLngBounds(
                    LatLng(it.latitude, it.longitude),
                    LatLng(mountainData.latitude, mountainData.longitude),
                )
            )
            naverMap.moveCamera(cameraUpdate)

            naverMap.locationOverlay.apply {
                isVisible = true
                position = LatLng(it.latitude, it.longitude)
                icon = OverlayImage.fromResource(R.drawable.ic_map_location)
                anchor = PointF(0.5f, 0f)
                subIcon = OverlayImage.fromResource(R.drawable.ic_marker_go)
                subAnchor = PointF(0.5f, 1f)
            }
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