package com.android.yaho.screen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.view.isVisible
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityReadyBinding
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.viewmodel.ReadyViewModel
import com.google.android.gms.location.*
import com.google.android.material.snackbar.Snackbar
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.component.KoinComponent

class ReadyActivity: BindingActivity<ActivityReadyBinding>(ActivityReadyBinding::inflate), KoinComponent  {

    private val TAG = this::class.simpleName

    companion object {
        const val KEY_SELECT_MOUNTAIN = "KEY_SELECT_MOUNTAIN"

        const val SCREEN_NEAR_MOUNTAIN = "SCREEN_NEAR_MOUNTAIN"
        const val SCREEN_SELECT_MOUNTAIN = "SCREEN_SELECT_MOUNTAIN"
        const val SCREEN_COUNT_DOWN = "SCREEN_COUNT_DOWN"
        const val SCREEN_GO_CLIMBING = "SCREEN_GO_CLIMBING"

        const val PERMISSION_REQUEST_CODE = 100
        val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }

    private val viewModel by viewModel<ReadyViewModel>()
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }
    private val locationRequest : LocationRequest by lazy { LocationRequest.create().apply {
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        viewModel.moveScreen(SCREEN_NEAR_MOUNTAIN)
        
        get<LiveClimbingCache>().clearCache()
        get<YahoPreference>().clearSelectedMountain()
    }

    override fun onStart() {
        super.onStart()
        viewModel.checkPermissions()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    getCurrentLocation()
                }
                else -> {
                    Snackbar.make(
                        binding.root, R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(
                            R.string.settings
                        ) { // Build intent that displays the App settings screen.
                            val intent = Intent()
                            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                            val uri = Uri.fromParts(
                                "package",
                                BuildConfig.APPLICATION_ID, null
                            )
                            intent.data = uri
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            startActivity(intent)
                        }
                        .show()
                }
            }

        } else {
            Toast.makeText(this, "위치 정보가 필요합니다.", Toast.LENGTH_SHORT).show()
            finish()
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
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
        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(result: LocationResult?) {
                val lastLocation = result?.lastLocation ?: return
                viewModel.getMyLocation(lastLocation)
            }
        }, null)
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initObserve() {
        viewModel.moveScreen.observe(this) { (screen, bundle) ->
            binding.toolbar.isVisible = true

            if(screen == SCREEN_GO_CLIMBING) {
                startActivity(Intent(this, ClimbingActivity::class.java).apply {
                    bundle?.let { putExtras(bundle) }
                })
                finish()
                return@observe
            }

            val fragment = when(screen) {
                SCREEN_NEAR_MOUNTAIN -> NearMountainFragment()
                SCREEN_SELECT_MOUNTAIN -> ReadyToStartFragment()
                SCREEN_COUNT_DOWN -> {
                    binding.toolbar.isVisible = false
                    CountDownFragment()
                }
                else -> NearMountainFragment()
            }.apply {
                arguments = bundle
            }

            supportFragmentManager.beginTransaction()
                .replace(binding.readyFragment.id, fragment)
                .commitAllowingStateLoss()
        }

        viewModel.checkPermissions.observe(this) {
            if(!it) {
                ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS,
                    PERMISSION_REQUEST_CODE
                )
            }
        }

        viewModel.clickNearMountainLocation.observe(this) {
            getCurrentLocation()
        }

        viewModel.clickMyCurrentLocation.observe(this) {
            getCurrentLocation()
        }
        
        viewModel.error.observe(this) {
            Toast.makeText(this@ReadyActivity, "Oops!!", Toast.LENGTH_SHORT).show()
            Log.w(TAG, "${it.message}")
        }
    }
}