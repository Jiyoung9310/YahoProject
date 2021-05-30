package com.climbing.yaho.local

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.climbing.yaho.R
import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.requestingLocationUpdates
import com.climbing.yaho.screen.ClimbingActivity
import com.climbing.yaho.screen.ClimbingActivity.Companion.KEY_IS_ACTIVE
import com.climbing.yaho.setRequestingLocationUpdates
import com.google.android.gms.location.*
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import java.text.DateFormat
import java.util.*


class LocationUpdatesService : Service(), KoinComponent {
    private val TAG = LocationUpdatesService::class.java.simpleName

    companion object {
        private const val CHANNEL_ID = "channel_01"
        private const val PACKAGE_NAME = "com.android.yaho.local"
        private const val NOTIFICATION_ID = 12345678
        const val EXTRA_LOCATION: String = "$PACKAGE_NAME.location"
        const val ACTION_BROADCAST = "$PACKAGE_NAME.broadcast"
        const val EXTRA_STARTED_FROM_NOTIFICATION = PACKAGE_NAME +
                ".started_from_notification"

        private const val UPDATE_INTERVAL_IN_MILLISECONDS: Long = 10000
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
        const val REQUEST_CODE = 1000
    }


    private val binder: IBinder = LocalBinder()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var myLocation : Location? = null
    private lateinit var notificationManager: NotificationManager
    private var changingConfiguration = false
    private lateinit var serviceHandler: Handler
    private val notiText : String by lazy { getString(
        R.string.noti_text_message, DateFormat.getDateTimeInstance().format(
            Date()
        )
    ) }

    private var isActive = true


    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                onNewLocation(locationResult.lastLocation)
            }
        }
        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        getLastLocation()

        val handler = HandlerThread(TAG)
        handler.start()
        serviceHandler = Handler(handler.looper)

        // Create the channel for the notification
        // Set the Notification Channel for the Notification Manager.
        notificationManager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                getString(R.string.app_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                setShowBadge(true)
                vibrationPattern = longArrayOf(0) // 진동 끄기
                enableVibration(true) // 진동 끄기
            }
        )
    }

    private fun getLastLocation() {
        try {
            fusedLocationClient.lastLocation
                .addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        myLocation = task.result
                    } else {
                        Log.w(TAG, "Failed to get location.")
                    }
                }
        } catch (unlikely: SecurityException) {
            Log.e(TAG, "Lost location permission.$unlikely")
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.i(TAG, "Service started")
        val startedFromNotification = intent.getBooleanExtra(
            EXTRA_STARTED_FROM_NOTIFICATION,
            false
        )

        // We got here because the user decided to remove location updates from the notification.
        if (startedFromNotification) {
            removeLocationUpdates()
            stopSelf()
            get<LiveClimbingCache>().clearCache()
            get<YahoPreference>().clearSelectedMountain()
        }
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY
    }

    private fun removeLocationUpdates() {
        Log.i(TAG, "Removing location updates")
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            setRequestingLocationUpdates(false)
            stopSelf()
            notificationManager.cancelAll()
        } catch (unlikely: SecurityException) {
            setRequestingLocationUpdates(true)
            Log.e(
                TAG,
                "Lost location permission. Could not remove updates. $unlikely"
            )
        }
    }
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        changingConfiguration = true
    }

    private fun onNewLocation(location: Location) {
        Log.i(TAG, "New location: $location")
        if(isActive) {
            get<LiveClimbingCache>().put(location, myLocation?.distanceTo(location))
//        get<ClimbingSaveHelper>().savePoint(location, myLocation?.distanceTo(location))

            // Notify anyone listening for broadcasts about the new location.
            LocalBroadcastManager.getInstance(applicationContext).sendBroadcast(
                Intent(ACTION_BROADCAST).apply {
                    putExtra(EXTRA_LOCATION, location)
                }
            )

            // Update notification content if running as a foreground service.
//        if (serviceIsRunningInForeground(this)) {
            notificationManager.notify(NOTIFICATION_ID, getNotification())
//        }
        }
        myLocation = location
    }

    private fun getNotification(): Notification? {

        val notiTitle = if(isActive) {
            getString(R.string.noti_title_climbing,
                get<LiveClimbingCache>().getRecord().mountainName
            )
        } else {
            getString(R.string.noti_title_resting)
        }
        // Extra to help us figure out if we arrived in onStartCommand via the notification or not.


        // The PendingIntent that leads to a call to onStartCommand() in this service.
        val servicePendingIntent = PendingIntent.getService(
            this, 0,
            Intent(this, LocationUpdatesService::class.java).apply {
                putExtra(EXTRA_STARTED_FROM_NOTIFICATION, true)
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        // The PendingIntent to launch activity.
        val activityPendingIntent = PendingIntent.getActivity(
            this, REQUEST_CODE,
            Intent(this, ClimbingActivity::class.java), 0
        )
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .addAction(
                R.drawable.ic_yaho, getString(R.string.launch_activity),
                activityPendingIntent
            )
            .addAction(
                R.drawable.ic_back, getString(R.string.remove_location_updates),
                servicePendingIntent
            )
            .setContentText(notiText)
            .setContentTitle(notiTitle)
            .setOngoing(true)
            .setPriority(NotificationManagerCompat.IMPORTANCE_LOW)
            .setVibrate(longArrayOf(0))
            .setSmallIcon(R.drawable.ic_yaho)
            .setTicker(notiText)
            .setWhen(System.currentTimeMillis())

        // Set the Channel ID for Android O.
        builder.setChannelId(CHANNEL_ID)
        return builder.build()
    }

    private fun serviceIsRunningInForeground(context: Context): Boolean {
        val manager = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (javaClass.name == service.service.className) {
                if (service.foreground) {
                    return true
                }
            }
        }
        return false
    }

    override fun onBind(intent: Intent?): IBinder {
        stopForeground(true)
        changingConfiguration = false
        return binder
    }

    override fun onRebind(intent: Intent?) {
        // Called when a client (MainActivity in case of this sample) returns to the foreground
        // and binds once again with this service. The service should cease to be a foreground
        // service when that happens.
        Log.i(TAG, "in onRebind()")
//        stopForeground(true)
        changingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(TAG, "Last client unbound from service")

        // Called when the last client (MainActivity in case of this sample) unbinds from this
        // service. If this method is called due to a configuration change in MainActivity, we
        // do nothing. Otherwise, we make this service a foreground service.
        if (!changingConfiguration && requestingLocationUpdates()) {
            Log.i(TAG, "Starting foreground service")
            startForeground(NOTIFICATION_ID, getNotification())
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        serviceHandler.removeCallbacksAndMessages(null)
        removeLocationUpdates()
        stopSelf()
//        get<LiveClimbingCache>().done()
    }

    fun requestLocationUpdates() {
        Log.i(TAG, "Requesting location updates")
        this.setRequestingLocationUpdates(true)
        startService(Intent(applicationContext, LocationUpdatesService::class.java))
        try {
            fusedLocationClient.requestLocationUpdates(
                LocationRequest.create().apply {
                    interval = UPDATE_INTERVAL_IN_MILLISECONDS
                    fastestInterval = FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS
                    priority = LocationRequest.PRIORITY_HIGH_ACCURACY
                },
                locationCallback, Looper.myLooper()
            )
        } catch (unlikely: SecurityException) {
            this.setRequestingLocationUpdates(false)
            Log.e(TAG, "Lost location permission. Could not request updates. $unlikely")
        }
    }

    fun serviceStop() {
        serviceHandler.removeCallbacksAndMessages(null)
        stopForeground(true)
        removeLocationUpdates()
        stopSelf()
    }

    fun isPause() {
        isActive = false
        notificationManager.notify(NOTIFICATION_ID, getNotification())
    }

    fun restart() {
        isActive = true
    }

    inner class LocalBinder : Binder() {
        fun getService() : LocationUpdatesService {
            return this@LocationUpdatesService
        }
    }
}