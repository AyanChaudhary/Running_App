package com.example.navigationapp.other

import android.graphics.Color

object Constants {

    const val ACTION_START_OR_RESUME_SERVICE="ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE="ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE="ACTION_STOP_SERVICE"


    const val LOCATION_UPDATE_INTERVAL=5000L
    const val FASTEST_LOCATION_UPDATE_INTERVAL=2000L
    const val LOCATION_MAXIMUM_DELAY_INTERVAL=7000L

    const val SHARED_PREFERENCES_NAME="shared_Pref"
    const val KEY_FIRST_TIME_TOGGLE="KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME="KEY_NAME"
    const val KEY_WEIGHT="KEY_WEIGHT"

    const val TIMER_DELAY_INTERVAL=50L
    const val POLYLINE_COLOR=Color.RED
    const val POLYLINE_WIDTH=8f
    const val CAMERA_ZOOM=16f
    const val NOTIFICATION_CHANNEL_ID="Tracking_service"
    const val NOTIFICATION_CHANNEL_NAME="Tracking"
    const val NOTIFICATION_ID=1
    const val ACTION_SHOW_TRACKING_FRAGMENT="ACTION_SHOW_TRACKING_FRAGMENT"
}