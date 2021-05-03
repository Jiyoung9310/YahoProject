package com.android.yaho.local

import android.content.Context
import android.location.Location
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.setRequestingLocationUpdates
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class LocationWorker(private val context: Context, workerParams: WorkerParameters)
    : Worker(context, workerParams), KoinComponent {

    private val TAG = this::class.java.simpleName

    companion object {
        private const val KEY_LATITUDE = "KEY_LATITUDE"
        private const val KEY_LONGITUDE ="KEY_LONGITUDE"
        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private var myLocation : Location? = null

    override fun doWork(): Result {
/*
        val latitude = inputData.getDouble(KEY_LATITUDE, 37.413294)
        val longitude = inputData.getDouble(KEY_LONGITUDE, 126.734086)
        */
        try {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = UPDATE_INTERVAL_IN_MILLISECONDS
                    fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult) {
                        super.onLocationResult(locationResult)
                        onNewLocation(locationResult.lastLocation)
                    }
                }, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            Toast.makeText(context, "Lost location permission.", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }


        return Result.success()
    }

    private fun onNewLocation(location: Location) {
        myLocation = location
        get<LiveClimbingCache>().put(location, myLocation?.distanceTo(location))

    }
}