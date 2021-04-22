package com.android.yaho.screen

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityMainBinding
import com.android.yaho.getLocationResultText
import com.android.yaho.viewmodel.MainViewModel
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.OverlayImage
import com.naver.maps.map.overlay.PathOverlay
import org.koin.androidx.viewmodel.ext.android.viewModel

class MainActivity : BindingActivity<ActivityMainBinding>(ActivityMainBinding::inflate), OnMapReadyCallback {
    private val TAG = this::class.java.simpleName

    private val viewModel by viewModel<MainViewModel>()
    private lateinit var naverMap: NaverMap

    private val firebaseAuth = Firebase.auth

    companion object {
        private const val LOCATION_REQUEST_INTERVAL = 10000
        private const val LOCATION_REQUEST_FAST_INTERVAL = 5000
        private const val PERMISSION_REQUEST_CODE = 100
        private val PERMISSIONS = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    }
    private val fusedLocationClient: FusedLocationProviderClient by lazy {
        LocationServices.getFusedLocationProviderClient(this)
    }

    private var waiting = false
    private var locationEnabled = false
    private var trackingEnabled = false

    private val locationList = mutableListOf<Location>()
    private val path = mutableListOf<LatLng>()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult?) {
            val lastLocation = locationResult?.lastLocation ?: return
            locationResult.locations.let { list ->
                locationList.addAll(list)
                path.addAll(list.map { LatLng(it) })
            }
            binding.tvLog.text = locationList.getLocationResultText()

            val coord = LatLng(lastLocation)
            val locationOverlay = naverMap.locationOverlay
            locationOverlay.position = coord
            locationOverlay.bearing = lastLocation.bearing
            naverMap.moveCamera(CameraUpdate.scrollTo(coord))
            if (waiting) {
                waiting = false
                binding.fab.setImageResource(R.drawable.ic_location_disabled_black_24dp)
                locationOverlay.isVisible = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportActionBar?.let {
            it.setDisplayHomeAsUpEnabled(true)
            it.setDisplayShowHomeEnabled(true)
        }

        initObserve()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapFragment?
            ?: MapFragment.newInstance(
                NaverMapOptions()
                    .backgroundResource(NaverMap.DEFAULT_BACKGROUND_DRWABLE_DARK)
                    .mapType(NaverMap.MapType.Terrain)
                    .enabledLayerGroups(NaverMap.LAYER_GROUP_MOUNTAIN)
                    .minZoom(4.0)
            ).also {
                supportFragmentManager.beginTransaction().add(R.id.mapFragment, it).commit()
            }

        binding.fab.setImageResource(R.drawable.ic_my_location_black_24dp)
        mapFragment.getMapAsync(this)

        binding.btnLogin.setOnClickListener {
            if(firebaseAuth.currentUser == null) {
                startActivity(Intent(this@MainActivity, LoginActivity::class.java))
            } else {
                Firebase.auth.signOut()
                binding.btnLogin.text = "LOGIN"
            }
        }

        binding.btnFind.setOnClickListener {
            locationList.getOrNull(0)?.let { viewModel.getNearByMountain(it.latitude, it.longitude) }
        }
    }

    private fun initObserve() {
        viewModel.nearByList.observe(this) {
            Log.d(TAG, "nearByList : $it")
            AlertDialog.Builder(this)
                .setTitle("선택")
                .setItems(it.map { it.name }.toTypedArray()) { dialogInterface, i ->
                    Toast.makeText(this@MainActivity, "near by : " + it[i], Toast.LENGTH_LONG)
                        .show()
                }
                .setNeutralButton("닫기", null)
                .setPositiveButton("확인", null)
                .show()

        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "Oops!! error : ${it.message}", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Oops!! error : ${it.message}")
        }

        viewModel.naverMap.observe(this) {
            /*binding.fab.setImageDrawable(CircularProgressDrawable(this).apply {
                setStyle(CircularProgressDrawable.LARGE)
                setColorSchemeColors(Color.WHITE)
                start()
            })
            tryEnableLocation()*/
        }
    }

    override fun onStart() {
        super.onStart()
        if (trackingEnabled) {
            enableLocation()
        }
    }

    override fun onResume() {
        super.onResume()

        firebaseAuth.currentUser?.let { user ->
            binding.btnLogin.text = "LOGOUT"
            Log.d(TAG, "firebaseAuth UID  : ${user.uid}")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        if (item.itemId == android.R.id.home) {
            finish()
            true
        } else {
            super.onOptionsItemSelected(item)
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PermissionChecker.PERMISSION_GRANTED }) {
                enableLocation()
            } else {
                binding.fab.setImageResource(R.drawable.ic_my_location_black_24dp)
            }
            return
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun enableLocation() {
        GoogleApiClient.Builder(this)
            .addConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                @SuppressLint("MissingPermission")
                override fun onConnected(bundle: Bundle?) {
                    fusedLocationClient.requestLocationUpdates(
                        LocationRequest.create().apply {
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                        interval = LOCATION_REQUEST_INTERVAL.toLong()
                        fastestInterval = LOCATION_REQUEST_FAST_INTERVAL.toLong()
                    }, locationCallback, null)

                    locationEnabled = true
                    waiting = true
                }

                override fun onConnectionSuspended(i: Int) {
                }
            })
            .addApi(LocationServices.API)
            .build()
            .connect()
    }


    override fun onMapReady(naverMap: NaverMap) {
        this.naverMap = naverMap

        val width = resources.getDimensionPixelSize(R.dimen.path_overlay_width)
        val outlineWidth = resources.getDimensionPixelSize(R.dimen.path_overlay_outline_width)

        if (path.size > 2) {
            PathOverlay().also {
                it.coords = path
                it.width = width
                it.outlineWidth = 0
                it.color = Color.BLUE
                it.patternImage = OverlayImage.fromResource(R.drawable.ic_path_arrow)
                it.patternInterval =
                    resources.getDimensionPixelSize(R.dimen.overlay_pattern_interval)
                it.map = naverMap
            }
        }

        viewModel.readyToStart(naverMap)

        binding.fab.setOnClickListener {
            if (trackingEnabled) {
                disableLocation()
                binding.fab.setImageResource(R.drawable.ic_my_location_black_24dp)
            } else {
                binding.fab.setImageDrawable(CircularProgressDrawable(this).apply {
                    setStyle(CircularProgressDrawable.LARGE)
                    setColorSchemeColors(Color.WHITE)
                    start()
                })
                tryEnableLocation()
            }
            trackingEnabled = !trackingEnabled
        }
    }

    private fun disableLocation() {
        if (!locationEnabled) {
            return
        }
        fusedLocationClient.removeLocationUpdates(locationCallback)
        locationEnabled = false
    }

    private fun tryEnableLocation() {
        if (PERMISSIONS.all { ContextCompat.checkSelfPermission(this, it) == PermissionChecker.PERMISSION_GRANTED }) {
            enableLocation()
        } else {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_REQUEST_CODE)
        }
    }

    override fun onStop() {
        super.onStop()
        disableLocation()
    }

}