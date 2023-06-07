package com.example.navigationapp.other

import android.content.Context
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.navigationapp.Model.Run
import com.example.navigationapp.R
import com.example.navigationapp.databinding.MarkerViewBinding
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CustomMarkerView( val runs:List<Run>,c:Context, layoutId:Int):MarkerView(c,layoutId) {

    val tvCalories=findViewById<TextView>(R.id.MtvCaloriesBurned)
    val tvDate=findViewById<TextView>(R.id.MtvDate)
    val tvDistance=findViewById<TextView>(R.id.MtvDistance)
    val tvTime=findViewById<TextView>(R.id.MtvDuration)
    val tvAvgSpeed=findViewById<TextView>(R.id.MtvAvgSpeed)


    override fun getOffset(): MPPointF {
        return MPPointF(-width/2f,-height.toFloat())
    }
    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        super.refreshContent(e, highlight)
        if(e==null)return
        val currId=e.x.toInt()
        val run=runs[currId]
        tvAvgSpeed.text=run.avgSpeedInKMH.toString()+"Km/h"
        tvCalories.text=run.caloriesBurned.toString()+"kCal"
        val calendar= Calendar.getInstance().apply {
            timeInMillis=run.timestamp
        }
        val sdf= SimpleDateFormat("dd.MM.yy", Locale.getDefault())
        tvDate.text=sdf.format(calendar.time)
        tvDistance.text="${run.distanceInMeters/1000f}km"
        tvTime.text=TrackingUtitlity.getFormattedTimeForStopWatch(run.timeInMillis)
    }
}