package com.android.yaho.repository

import android.os.Looper
import com.android.yaho.checkLocationPermission
import com.android.yaho.di.ContextDelegate
import com.android.yaho.isGPSEnabled
import com.android.yaho.local.LocationWorker
import com.android.yaho.local.db.LiveClimbingDao
import com.android.yaho.local.db.PointEntity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

interface LocationRepository  {
    fun saveLocation()
}

class LocationRepositoryImpl(
    private val contextDelegate: ContextDelegate,
    private val climbingDao: LiveClimbingDao,
) : LocationRepository {

    companion object {
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    override fun saveLocation() {
        if (contextDelegate.getContext().isGPSEnabled()
            && contextDelegate.getContext().checkLocationPermission()) {
            LocationServices.getFusedLocationProviderClient(contextDelegate.getContext())
                .requestLocationUpdates(
                    LocationRequest.create().apply {
                        interval = UPDATE_INTERVAL_IN_MILLISECONDS
                        fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
                        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                    },
                    object : LocationCallback() {
                        override fun onLocationResult(locationResult: LocationResult) {
                            super.onLocationResult(locationResult)
                            val newLocation = locationResult.lastLocation
                            GlobalScope.launch {
                                climbingDao.insertPoint(
                                    PointEntity(
                                        latitude = newLocation.latitude,
                                        longitude = newLocation.longitude,
                                        altitude = newLocation.altitude,
                                        speed = newLocation.speed,
                                        timestamp = newLocation.time
                                    )
                                )
                            }
                        }
                    }, Looper.myLooper()
                )
        }
    }

}