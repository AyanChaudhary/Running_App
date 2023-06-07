package com.example.navigationapp.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.navigationapp.R
import com.example.navigationapp.databinding.FragmentStatisticsBinding
import com.example.navigationapp.other.CustomMarkerView
import com.example.navigationapp.other.TrackingUtitlity
import com.example.navigationapp.ui.viewmodels.MainViewModel
import com.example.navigationapp.ui.viewmodels.StatisticViewModel
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel : StatisticViewModel by viewModels()
    lateinit var binding: FragmentStatisticsBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding=FragmentStatisticsBinding.bind(view)
        setUpBarChart()
        subscribeToObservers()
    }

    private fun setUpBarChart(){
        binding.barChart.xAxis.apply {
            position=XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor= Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.apply {
            axisLineColor= Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
        binding.barChart.axisRight.apply {
            axisLineColor= Color.WHITE
            textColor=Color.WHITE
            setDrawGridLines(false)
        }
    }

    private fun subscribeToObservers(){
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun=TrackingUtitlity.getFormattedTimeForStopWatch(it)
                binding.tvTotalTime.text=totalTimeRun
            }
        })
        viewModel.totalDistanceRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalDistInKm=it/1000f
                val finalTotDist= round(totalDistInKm*10f)/10f
                binding.tvTotalDistance.text="${finalTotDist}km"
            }
        })
        viewModel.totalAvgSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val avgSpeed= round(it*10f)/10f
                binding.tvAverageSpeed.text="${avgSpeed}km/h"
            }
        })
        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                binding.tvTotalCalories.text="${it}kCal"
            }
        })
        viewModel.runsSortedByDate.observe(viewLifecycleOwner, Observer {
            it?.let {
                val allAvgSpeed=it.indices.map { i->BarEntry(i.toFloat(),it[i].avgSpeedInKMH) }
                val barDataSet=BarDataSet(allAvgSpeed,"Avg Speed Over Time").apply {
                    valueTextColor=Color.WHITE
                    color=ContextCompat.getColor(requireContext(),R.color.colorAccent)
                }
                binding.barChart.data= BarData(barDataSet)
                binding.barChart.marker=CustomMarkerView(it.reversed(),requireContext(),R.layout.marker_view)
                binding.barChart.invalidate()
            }
        })
    }
}