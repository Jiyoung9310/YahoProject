package com.android.yaho.screen

import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.preference.PreferenceManager
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.view.isInvisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.android.yaho.*
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.data.MountainData
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.databinding.ActivityClimbingBinding
import com.android.yaho.local.LocationUpdatesService
import com.android.yaho.local.LocationWorker
import com.android.yaho.viewmodel.ClimbingViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.naver.maps.geometry.LatLng
import com.naver.maps.geometry.LatLngBounds
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class ClimbingActivity : BindingActivity<ActivityClimbingBinding>(ActivityClimbingBinding::inflate),
    OnMapReadyCallback, OnSharedPreferenceChangeListener {

    private val TAG = this::class.java.simpleName

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        const val KEY_MOUNTAIN_DATA = "KEY_MOUNTAIN_DATA"
    }

    private val viewModel by viewModel<ClimbingViewModel>()
    private var naverMap : NaverMap? = null
    private lateinit var pathOverlay: PathOverlay

    private val receiver = MyReceiver()
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isBound = false
    private lateinit var mountainData : MountainData

    private val behavior by lazy { BottomSheetBehavior.from(binding.clBottom) }

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
        val data = intent.extras?.getParcelable<MountainData>(KEY_MOUNTAIN_DATA)
        if(data != null) {
            mountainData = data
            get<LiveClimbingCache>().initialize(mountainData.id)
//            get<ClimbingSaveHelper>().init(mountainData.id)

            startWork()
        }

        get<LiveClimbingCache>().initialize(mountainData.id)
        initView()
        initObserve()
    }

    private fun startWork() {
        // 입력 데이터를 설정합니다. Bundle와 동일합니다.
        val work = createWorkRequest(Data.EMPTY)

        /* 작업을 큐에 넣을때 동일한 작업인 경우에 대한 정책을 지정할 수 있습니다.
        ExistingPeriodicWorkPolicy.KEEP은 동일한 작업을 큐에 넣게되며,
        ExistingPeriodicWorkPolicy.REPLACE인 경우 작업이 대체됩니다. */
        WorkManager.getInstance(this).enqueueUniquePeriodicWork("Smart work", ExistingPeriodicWorkPolicy.KEEP, work)

        // 작업의 상태를 LiveData를 통해 관찰하게 됩니다.
        WorkManager.getInstance(this).getWorkInfoByIdLiveData(work.id)
            .observe(this, { workInfo ->
                if (workInfo != null && workInfo.state == WorkInfo.State.SUCCEEDED) {
                    // 작업 완료
                }
            })
    }

    private fun createWorkRequest(data: Data) = PeriodicWorkRequestBuilder<LocationWorker>(5, TimeUnit.SECONDS)
        .setInputData(data) // 입력 데이터
        .setConstraints(createConstraints()) // 작업을 재시도 할경우에 대한 정책
        .setBackoffCriteria(BackoffPolicy.LINEAR,
            PeriodicWorkRequest.MIN_BACKOFF_MILLIS, TimeUnit.MILLISECONDS)
        .build()

    private fun createConstraints() = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        // 다른값(NOT_REQUIRED, CONNECTED, NOT_ROAMING, METERED)
        .setRequiresBatteryNotLow(true)                 // 배터리가 부족하지 않는 경우
        .setRequiresStorageNotLow(true)                 // 저장소가 부족하지 않는 경우
        .build()

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

        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // slide
                binding.bottomComponent.isInvisible = slideOffset < 0.3
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // state changed
            }
        })
    }

    private fun initObserve() {
        viewModel.boundService.observe(this) {
            if(it) locationUpdatesService?.requestLocationUpdates()
        }

        viewModel.climbingData.observe(this) {
            binding.tvDistance.text = getString(R.string.kilo_meter_unit, it.allDistance.toString())
            binding.tvHeight.text = getString(R.string.meter_unit, it.height.toString())
        }

        viewModel.updateMap.observe(this) {
            updateMapMarker(it.latitude, it.longitude)
        }

        viewModel.runningTime.observe(this) {
            val hour = it.secondsToHour()
            val min = it.secondsToMinute()
            val sec = it.secondsToSec()
            binding.tvRunningTime.text = String.format("%d시간 %02d분 %02d초", hour, min, sec)
        }
    }

    private fun updateMapMarker(latitude: Double, longitude: Double) {
        val cameraUpdate = CameraUpdate.fitBounds(
            LatLngBounds(
                LatLng(latitude, longitude),
                LatLng(mountainData.latitude, mountainData.longitude),
            )
            , 150)
        naverMap?.moveCamera(cameraUpdate)

        naverMap?.locationOverlay?.apply {
            isVisible = true
            position = LatLng(latitude, longitude)
            icon = OverlayImage.fromResource(R.drawable.ic_map_location)
        }

        with(get<LiveClimbingCache>().latlngPaths) {
            if(this.size < 2) return@with
            pathOverlay.coords = this
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

        Marker().apply {
            position = LatLng(mountainData.latitude, mountainData.longitude)
            icon = OverlayImage.fromResource(R.drawable.ic_marker_goal_flag)
            map = naverMap
        }

        pathOverlay.also {
            it.width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
            it.outlineWidth = 0
            it.color = Color.BLACK
            it.map = naverMap
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val lastLocation = task.result
                        naverMap.apply {
                            updateMapMarker(lastLocation.latitude, lastLocation.longitude)
                        }
                    } else {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
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
                viewModel.updateCurrentLocation()
                Toast.makeText(
                    context, location.getLocationResultText(),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}