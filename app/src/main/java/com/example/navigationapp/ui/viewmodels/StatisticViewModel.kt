package com.example.navigationapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.navigationapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StatisticViewModel @Inject constructor(val mainRepository: MainRepository) : ViewModel() {

    val totalTimeRun=mainRepository.getTotalTimeInMillis()
    val totalDistanceRun=mainRepository.getTotalDistance()
    val totalCaloriesBurned=mainRepository.getTotalCaloriesBurned()
    val totalAvgSpeed=mainRepository.getTotalAvgSpeed()

    val runsSortedByDate=mainRepository.getAllRunsSortedByDate()
}