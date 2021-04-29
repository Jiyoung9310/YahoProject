package com.android.yaho.screen

import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.yaho.*
import com.android.yaho.base.BindingActivity
import com.android.yaho.data.cache.LiveClimbingCache
import com.android.yaho.databinding.ActivityClimbingBinding
import com.android.yaho.getLocationText
import com.android.yaho.local.LocationUpdatesService
import com.android.yaho.viewmodel.ClimbingViewModel
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.map.MapFragment
import com.naver.maps.map.NaverMap
import com.naver.maps.map.NaverMapOptions
import com.naver.maps.map.OnMapReadyCallback
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingActivity : BindingActivity<ActivityClimbingBinding>(ActivityClimbingBinding::inflate),
    OnMapReadyCallback, OnSharedPreferenceChangeListener {

    private val TAG = this::class.java.simpleName

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        const val KEY_MOUNTAIN_ID = "KEY_MOUNTAIN_ID"
    }

    private val viewModel by viewModel<ClimbingViewModel>()

    private val receiver = MyReceiver()
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isBound = false
    private val mountainId : Int by lazy { intent.extras?.getInt(KEY_MOUNTAIN_ID,0) ?: 0 }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationUpdatesService.LocalBinder = service as LocationUpdatesService.LocalBinder
            locationUpdatesService = binder.getService()
            isBound = true
            viewModel.onSettingService(isBound)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            locationUpdatesService = null
            isBound = false
            viewModel.onSettingService(isBound)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        get<LiveClimbingCache>().initialize(mountainId)
        initView()
        initObserve()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.

        bindService(
            Intent(this, LocationUpdatesService::class.java), serviceConnection,
            BIND_AUTO_CREATE
        )
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver,
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
    }

    override fun onStop() {
        super.onStop()
        if (isBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(serviceConnection)
            isBound = false
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, s: String?) {
        // Update the buttons state depending on whether location updates are being requested.
        if (s == KEY_REQUESTING_LOCATION_UPDATES) {

        }
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

    private fun initObserve() {
        viewModel.boundService.observe(this) {
            if(it) locationUpdatesService?.requestLocationUpdates()
        }
    }

    override fun onMapReady(naverMap: NaverMap) {

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        Log.i(TAG, "onRequestPermissionResult")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    // If user interaction was interrupted, the permission request is cancelled and you
                    // receive empty arrays.
                    Log.i(TAG, "User interaction was cancelled.")
                }
                grantResults[0] == PackageManager.PERMISSION_GRANTED -> {
                    // Permission was granted.
                    locationUpdatesService?.requestLocationUpdates()
                }
                else -> {
                    // Permission denied.
                    Snackbar.make(
                        binding.root,
                        R.string.permission_denied_explanation,
                        Snackbar.LENGTH_INDEFINITE
                    )
                        .setAction(R.string.settings) { // Build intent that displays the App settings screen.
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
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    inner class MyReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val location =
                intent.getParcelableExtra<Location>(LocationUpdatesService.EXTRA_LOCATION)
            if (location != null) {
                Toast.makeText(
                    context, location.getLocationResultText(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}