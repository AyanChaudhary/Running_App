package com.example.navigationapp.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.navigationapp.Model.Run
import com.example.navigationapp.R
import com.example.navigationapp.databinding.ItemRunBinding
import com.example.navigationapp.other.TrackingUtitlity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter: RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(val binding: ItemRunBinding):RecyclerView.ViewHolder(binding.root)

    val diffCallBack= object :DiffUtil.ItemCallback<Run>(){
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }
        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode()==newItem.hashCode()
        }
    }
    var differList=AsyncListDiffer(this,diffCallBack)

    fun submitList(list : List<Run>)= differList.submitList(list)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(ItemRunBinding.inflate(LayoutInflater.from(parent.context),parent,false))
    }

    override fun getItemCount(): Int=differList.currentList.size

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run=differList.currentList[position]
        holder.binding.apply {
            Glide.with(root.context).load(run.img).into(ivRunImage)
            tvAvgSpeed.text=run.avgSpeedInKMH.toString()+"Km/h"
            tvCalories.text=run.caloriesBurned.toString()+"kCal"
            val calendar=Calendar.getInstance().apply {
                timeInMillis=run.timestamp
            }
            val sdf=SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text=sdf.format(calendar.time)
            tvDistance.text="${run.distanceInMeters/1000f}km"
            tvTime.text=TrackingUtitlity.getFormattedTimeForStopWatch(run.timeInMillis)
        }
    }
}