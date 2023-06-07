package com.example.navigationapp.other

import android.Manifest
import android.content.Context
import android.location.Location
import android.os.Build
import com.example.navigationapp.services.polyline
import com.example.navigationapp.services.polylines
import pub.devrel.easypermissions.EasyPermissions
import java.sql.Time
import java.util.concurrent.TimeUnit

object TrackingUtitlity {

    fun hasLocationPermissions(context:Context)=
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            EasyPermissions.hasPermissions(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }
        else{
                EasyPermissions.hasPermissions(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                )
            }

    fun getFormattedTimeForStopWatch(ms:Long,includeMillis:Boolean=false):String{
        var millisTime=ms
        val hours=TimeUnit.MILLISECONDS.toHours(millisTime)
        millisTime-=TimeUnit.HOURS.toMillis(hours)
        val minutes=TimeUnit.MILLISECONDS.toMinutes(millisTime)
        millisTime-=TimeUnit.MINUTES.toMillis(minutes)
        val seconds=TimeUnit.MILLISECONDS.toSeconds(millisTime)
        millisTime-=TimeUnit.SECONDS.toMillis(seconds)
        millisTime/=10

        if(!includeMillis){
            return "${if(hours < 10) "0" else ""}$hours:"+
                    "${if(minutes<10) "0" else ""}$minutes:"+
                    "${if(seconds<10) "0" else ""}$seconds"
        }
        else{
            return "${if(hours < 10) "0" else ""}$hours:"+
                    "${if(minutes<10) "0" else ""}$minutes:"+
                    "${if(seconds<10) "0" else ""}$seconds:"+
                    "${if(millisTime<10) "0" else ""}$millisTime"
        }
    }

    fun calculatePolylineLength(polyline: polyline):Float{
        var dist=0f
        for( i in 0..polyline.size-2){
            val pos1=polyline[i]
            val pos2=polyline[i+1]
            val result=FloatArray(1)
            Location.distanceBetween(
                pos1.latitude,pos1.longitude,
                pos2.latitude,pos2.longitude,
                result
            )
            dist+=result[0]
        }
        return dist
    }

}