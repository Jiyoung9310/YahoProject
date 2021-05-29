package com.android.yaho.screen

import android.app.PendingIntent
import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PointF
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
import androidx.core.view.isVisible
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.*
import com.android.yaho.*
import com.android.yaho.BuildConfig
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.data.MountainData
import com.android.yaho.databinding.ActivityClimbingBinding
import com.android.yaho.local.LocationUpdatesService
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.local.cache.MountainListCache
import com.android.yaho.ui.ClimbingDoneDialog
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

class ClimbingActivity : BindingActivity<ActivityClimbingBinding>(ActivityClimbingBinding::inflate),
    OnMapReadyCallback, OnSharedPreferenceChangeListener {

    private val TAG = this::class.java.simpleName

    companion object {
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
        const val KEY_MOUNTAIN_DATA = "KEY_MOUNTAIN_DATA"
        const val KEY_MOUNTAIN_VISIT_COUNT = "KEY_MOUNTAIN_VISIT_COUNT"
        const val KEY_IS_ACTIVE = "KEY_IS_ACTIVE"
        private val KEY_TIME_STAMP = "KEY_TIME_STAMP"
        private val KEY_RUNNING_TIME = "KEY_RUNNING_TIME"
    }

    private val viewModel by viewModel<ClimbingViewModel>()
    private var naverMap: NaverMap? = null
    private val pathOverlay: PathOverlay by lazy { PathOverlay() }

    private val receiver = MyReceiver()
    private var locationUpdatesService: LocationUpdatesService? = null
    private var isBound = false
    private lateinit var mountainData: MountainData
    private var runningTime: Long = 0
    private var isActive = true

    private val behavior by lazy { BottomSheetBehavior.from(binding.clBottom) }

    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            val binder: LocationUpdatesService.LocalBinder =
                service as LocationUpdatesService.LocalBinder
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

    override fun startActivityForResult(intent: Intent?, requestCode: Int, options: Bundle?) {
        super.startActivityForResult(intent, requestCode, options)
        if (requestCode == LocationUpdatesService.REQUEST_CODE) {
            intent?.getParcelableExtra<MountainData>(KEY_MOUNTAIN_DATA)?.let {
                mountainData = it
            }
            /*intent?.getBooleanExtra(KEY_IS_ACTIVE, true)?.let {
                isActive = it
                showBottomView(isActive)
                if(it) viewModel.updateCurrentLocation()
            }*/
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (get<YahoPreference>().selectedMountainId > 0) {
            get<MountainListCache>().get(get<YahoPreference>().selectedMountainId)?.let {
                mountainData = it
            }
            isActive = get<YahoPreference>().isActive
            showBottomView(isActive)
            if(isActive) viewModel.updateCurrentLocation()

            get<YahoPreference>().runningTimeStamp.let { timeStamp ->
                if (timeStamp > 0) {
                    val runningCount = get<YahoPreference>().runningTimeCount
                    val restCount = get<YahoPreference>().restTimeCount
                    viewModel.setRunningTime(runningCount, restCount,
                        (System.currentTimeMillis() / 1000 - timeStamp),
                        isActive
                    )
                }
            }
        } else if(intent.extras?.getParcelable<MountainData>(KEY_MOUNTAIN_DATA) != null) {
            intent.extras?.getParcelable<MountainData>(KEY_MOUNTAIN_DATA)?.let {
                mountainData = it
                get<YahoPreference>().selectedMountainId = mountainData.id
            }
        } else {
            finish()
        }
        get<LiveClimbingCache>().initialize(mountainData, intent.getIntExtra(KEY_MOUNTAIN_VISIT_COUNT, 0))

        initView()
        initObserve()
    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        if (get<YahoPreference>().selectedMountainId > 0) {
            bindService(
                Intent(this, LocationUpdatesService::class.java), serviceConnection,
                BIND_AUTO_CREATE
            )
        } else {
            startActivity(Intent(applicationContext, HomeActivity::class.java).apply{
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            })
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            receiver,
            IntentFilter(LocationUpdatesService.ACTION_BROADCAST)
        )
        Log.i(TAG, "디버깅!!! onResume()")
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver)
        Log.i(TAG, "디버깅!!! onPause()")
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
        get<YahoPreference>().apply {
            runningTimeStamp = System.currentTimeMillis() / 1000
            if(isActive) runningTimeCount = runningTime else restTimeCount = runningTime
        }

        Log.i(TAG, "디버깅!!! onStop()")
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
                binding.tvRunningTime.isInvisible = slideOffset < 0.3
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                // state changed
            }
        })

        binding.btnPause.setOnClickListener {
            isActive = !isActive
            showBottomView(isActive)
            viewModel.onClickPause(isActive)
            if(isActive) {
                viewModel.updateCurrentLocation()
                locationUpdatesService?.restart()
                get<YahoPreference>().restTimeCount = runningTime
            } else {
                locationUpdatesService?.isPause()
                get<YahoPreference>().runningTimeCount = runningTime
            }
            get<YahoPreference>().isActive = isActive
        }

        binding.btnClimbingDone.setOnClickListener {
            ClimbingDoneDialog(this,
                climbingTimeText = binding.tvRunningTime.text.toString(),
                onClickGoal = {
                    viewModel.onClickDone()
                    locationUpdatesService?.serviceStop()
                }
            ).show()
        }
    }

    private fun showBottomView(isActive: Boolean) {
        binding.bottomActiveView.isVisible = isActive
        binding.tvRestTitle.isVisible = !isActive
        if(isActive) {
            binding.btnPause.setImageResource(R.drawable.ic_btn_pause)
        } else {
            binding.btnPause.setImageResource(R.drawable.ic_btn_play)
        }
    }

    private fun initObserve() {
        viewModel.boundService.observe(this) {
            if (it) locationUpdatesService?.requestLocationUpdates()
        }

        viewModel.climbingData.observe(this) {
            binding.tvDistance.text = it.allDistance.meter(this)
            binding.tvHeight.text = getString(R.string.meter_unit, it.height.toFloat())
        }

        viewModel.updateMap.observe(this) {
            updateMapMarker(it.latitude, it.longitude)
        }

        viewModel.runningTime.observe(this) {
            runningTime = it
            binding.tvRunningTime.text = if(isActive) {
                it.secondsToHourTimeFormat()
            } else {
                it.secondsToMinuteTimeFormat()
            }
        }

        viewModel.clickDone.observe(this) {
            if(it != null) {
                startActivity(Intent(this, ClimbingDoneActivity::class.java))
            } else {
                Toast.makeText(applicationContext, "저장할 등산 데이터가 없습니다.", Toast.LENGTH_SHORT).show()
            }

            finish()
        }

        viewModel.stampMarker.observe(this) {
            sectionMarker(it)
        }
    }

    private fun updateMapMarker(latitude: Double, longitude: Double) {

        naverMap?.locationOverlay?.apply {
            isVisible = true
            anchor = PointF(0.5f, 0.5f)
            position = LatLng(latitude, longitude)
            icon = OverlayImage.fromResource(R.drawable.img_marker_my_location)
        }

        get<LiveClimbingCache>().latlngPaths.let { list ->
            if (list.size < 2) return@let
            pathOverlay.apply {
                coords = list
                map = naverMap
            }
        }
    }

    private fun sectionMarker(latlng: LatLng) {
        if(isActive) {
            naverMap?.locationOverlay?.apply {
                anchor = PointF(0.5f, 0.5f)
                subIcon = null
            }
            Marker().apply {
                zIndex = 0
                position = latlng
                icon =
                    OverlayImage.fromResource(R.drawable.img_marker_dot)
                anchor = PointF(0.1f, 0.7f)
                isForceShowIcon = true
                map = naverMap
            }
        } else {
            naverMap?.locationOverlay?.apply {
                anchor = PointF(0.5f, 0f)
                subIcon = OverlayImage.fromResource(R.drawable.img_marker_rest)
                subAnchor = PointF(0.5f, 1f)
            }
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap
        Log.i(TAG, "디버깅!!! onMapReady() : $naverMap")
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
            icon = OverlayImage.fromResource(R.drawable.img_marker_goal_flag)
            map = naverMap
        }

        pathOverlay.also {
            it.width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
            it.outlineWidth = resources.getDimensionPixelSize(R.dimen.path_overlay_outline_width)
            it.color = Color.BLACK
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        val lastLocation = task.result
                        naverMap.apply {
                            updateMapMarker(lastLocation.latitude, lastLocation.longitude)
                            val cameraUpdate = CameraUpdate.fitBounds(
                                LatLngBounds(
                                    LatLng(lastLocation.latitude, lastLocation.longitude),
                                    LatLng(mountainData.latitude, mountainData.longitude),
                                ), 150
                            )
                            naverMap.moveCamera(cameraUpdate)
                            sectionMarker(LatLng(lastLocation.latitude, lastLocation.longitude))
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
                /*Toast.makeText(
                    context, location.getLocationResultText(),
                    Toast.LENGTH_SHORT
                ).show()*/
            }
        }
    }
}