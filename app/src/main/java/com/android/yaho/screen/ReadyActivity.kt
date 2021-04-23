package com.android.yaho.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityReadyBinding
import com.android.yaho.viewmodel.ReadyViewModel
import com.google.android.gms.location.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReadyActivity: BindingActivity<ActivityReadyBinding>(ActivityReadyBinding::inflate)  {

    private val TAG = this::class.simpleName

    companion object {
        const val SCREEN_NEAR_MOUNTAIN = "SCREEN_NEAR_MOUNTAIN"
        const val SCREEN_SELECT_MOUNTAIN = "SCREEN_SELECT_MOUNTAIN"
        const val SCREEN_COUNT_DOWN = "SCREEN_COUNT_DOWN"

        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    private val viewModel by viewModel<ReadyViewModel>()
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        viewModel.moveScreen(SCREEN_NEAR_MOUNTAIN)
    }

    override fun onStart() {
        super.onStart()
        checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            checkPermissions()
            return
        } else {
            Toast.makeText(this, "위치 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun checkPermissions() {
        if (PERMISSIONS.all { ContextCompat.checkSelfPermission(this, it) == PermissionChecker.PERMISSION_GRANTED }) {
            getCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(this,
                PERMISSIONS,
                PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "위치 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        fusedLocationClient.requestLocationUpdates(
            LocationRequest.create().apply {
                priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            }, object: LocationCallback() {
                override fun onLocationResult(result: LocationResult?) {
                    val lastLocation = result?.lastLocation ?: return
                    viewModel.getNearMountain(lastLocation.latitude, lastLocation.longitude)
                }
            }, null)
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initObserve() {
        viewModel.moveScreen.observe(this) {
            val fragment = when(it) {
                SCREEN_NEAR_MOUNTAIN -> NearMountainFragment()
                SCREEN_SELECT_MOUNTAIN -> NearMountainFragment()
                SCREEN_COUNT_DOWN -> NearMountainFragment()
                else -> NearMountainFragment()
            }
            supportFragmentManager.beginTransaction()
                .replace(binding.readyFragment.id, fragment)
                .commitAllowingStateLoss()
        }

        viewModel.clickLocation.observe(this) {
            checkPermissions()
        }
        
        viewModel.error.observe(this) {
            Toast.makeText(this@ReadyActivity, "Oops!!", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "${it.message}")
        }
    }
}