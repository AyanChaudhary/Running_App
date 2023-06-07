package com.example.navigationapp.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.navigationapp.R
import com.example.navigationapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.navigationapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.navigationapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.navigationapp.other.Constants.ACTION_STOP_SERVICE
import com.example.navigationapp.other.Constants.FASTEST_LOCATION_UPDATE_INTERVAL
import com.example.navigationapp.other.Constants.LOCATION_MAXIMUM_DELAY_INTERVAL
import com.example.navigationapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.navigationapp.other.Constants.NOTIFICATION_CHANNEL_ID
import com.example.navigationapp.other.Constants.NOTIFICATION_CHANNEL_NAME
import com.example.navigationapp.other.Constants.NOTIFICATION_ID
import com.example.navigationapp.other.Constants.TIMER_DELAY_INTERVAL
import com.example.navigationapp.other.TrackingUtitlity
import com.example.navigationapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias polyline=MutableList<LatLng>
typealias polylines=MutableList<polyline>

@AndroidEntryPoint
class TrackingService : LifecycleService() {
    var isFirstRun=true;
    var serviceKilled=false
    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    @Inject
    lateinit var baseNotificationBuilder:NotificationCompat.Builder
    lateinit var currNotificationBuilder:NotificationCompat.Builder

    private val timeRunInSeconds=MutableLiveData<Long>()
    companion object{
        val timeRunInMillis=MutableLiveData<Long>()
        val isTracking=MutableLiveData<Boolean>()
        val pathPoints=MutableLiveData<polylines>()
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate() {
        super.onCreate()
        postInitialValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        currNotificationBuilder=baseNotificationBuilder
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTrackingState(it)
        })
    }
    private fun postInitialValues(){
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timeRunInSeconds.postValue(0L)
        timeRunInMillis.postValue(0L)
    }
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action){
                ACTION_START_OR_RESUME_SERVICE->{
                    if(isFirstRun){
                        isFirstRun=false
                        startForegroundService()
                    }
                    else {
                        Timber.d("resuming service")
                        startTimer()
                    }
                }
                ACTION_PAUSE_SERVICE->{
                    Timber.d("paused service")
                    pauseService()
                }
                ACTION_STOP_SERVICE->{
                    Timber.d("stoped service")
                    killService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }


    private var isTimerEnabled=false
    private var lapTime=0L
    private var totalRun=0L
    private var timeStarted=0L
    private var lastSecondTimeStamp=0L

    private fun startTimer(){
        addEmptyPolyLine()
        isTracking.postValue(true)
        isTimerEnabled=true
        // whenever we start or resume our stopwatch timestarted gets updated
        timeStarted=System.currentTimeMillis()
        CoroutineScope(Dispatchers.Main).launch {
            while(isTracking.value!!){
                // time passed since we started or resumed our stopwatch
                lapTime=System.currentTimeMillis()-timeStarted
                timeRunInMillis.postValue(totalRun+lapTime)
                // if 1+ second has passed we have to update timeRunInSeconds timer
                if(timeRunInMillis.value!! >= lastSecondTimeStamp+1000L){
                    timeRunInSeconds.postValue(timeRunInSeconds.value!!+1)
                    lastSecondTimeStamp+=1000L
                }
                delay(TIMER_DELAY_INTERVAL)
            }
            totalRun+=lapTime
        }
    }

    private fun updateNotificationTrackingState(isTracking: Boolean){
        val notificationActionText = if(isTracking)"Pause" else "Resume"
        val pendingIntent= if(isTracking){
            val pauseIntent=Intent(this,TrackingService::class.java).apply {
                action= ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this,1,pauseIntent, FLAG_IMMUTABLE)
        }else{
            val resumeIntent=Intent(this,TrackingService::class.java).apply {
                action= ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this,2,resumeIntent, FLAG_IMMUTABLE)
        }
        val notificationManager= getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        currNotificationBuilder.javaClass.getDeclaredField("mActions").apply {
            isAccessible=true
            set(currNotificationBuilder,ArrayList<NotificationCompat.Action>())
        }

        if(!serviceKilled){
            currNotificationBuilder=baseNotificationBuilder
                .addAction(R.drawable.baseline_pause_24,notificationActionText,pendingIntent)
            notificationManager.notify(NOTIFICATION_ID,currNotificationBuilder.build())
        }
    }
    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.S)
    private fun updateLocationTracking(isTracking:Boolean){
        if(isTracking){
            if(TrackingUtitlity.hasLocationPermissions(this)){
                val request = com.google.android.gms.location.LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY,
                LOCATION_UPDATE_INTERVAL).setWaitForAccurateLocation(false).setMinUpdateIntervalMillis(
                    FASTEST_LOCATION_UPDATE_INTERVAL).setMaxUpdateDelayMillis(
                    LOCATION_MAXIMUM_DELAY_INTERVAL).build()
                fusedLocationProviderClient.requestLocationUpdates(
                    request,locationCallback,Looper.getMainLooper()
                )
            }
        }else{
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }
    private fun killService(){
        serviceKilled=true
        isFirstRun=true
        pauseService()
        postInitialValues()
        stopForeground(true)
        stopSelf()
    }
    val locationCallback=object :LocationCallback(){
        override fun onLocationResult(result: LocationResult) {
            super.onLocationResult(result)
            if(isTracking.value!!){
                result?.locations?.let {locations->
                    for(location in locations){
                        addPathPoint(location)
                        Timber.d("New Location : ${location.latitude}, ${location.longitude}")
                    }
                }
            }
        }
    }
    private fun addEmptyPolyLine()= pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    }?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location:Location?){
        location?.let {
            val pos=LatLng(it.latitude,it.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }
    private fun pauseService(){
        isTracking.postValue(false)
        isTimerEnabled=false
    }
    private fun startForegroundService(){
        startTimer()
        isTracking.postValue(true)
        val notificationManager=getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            createNotificationChannel(notificationManager)
        }
        startForeground(NOTIFICATION_ID,baseNotificationBuilder.build())

        timeRunInSeconds.observe(this, Observer {
            if(!serviceKilled){
                val notification = currNotificationBuilder
                    .setContentText(TrackingUtitlity.getFormattedTimeForStopWatch(it * 1000))
                notificationManager.notify(NOTIFICATION_ID,notification.build())
            }
        })
    }


    private fun createNotificationChannel(notificationManager: NotificationManager){
        val channel=NotificationChannel(NOTIFICATION_CHANNEL_ID,
        NOTIFICATION_CHANNEL_NAME,
        IMPORTANCE_LOW)
        notificationManager.createNotificationChannel(channel)
    }
}