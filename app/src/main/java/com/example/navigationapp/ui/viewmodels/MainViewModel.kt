package com.example.navigationapp.ui.viewmodels

import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.navigationapp.Model.Run
import com.example.navigationapp.other.SortType
import com.example.navigationapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val mainRepository: MainRepository) : ViewModel() {

    fun insertRun(run: Run) = viewModelScope.launch {
        mainRepository.insertRun(run)
    }
    private val getRunsSortedByDate= mainRepository.getAllRunsSortedByDate()
    private val getRunsSortedByDistance=mainRepository.getAllRunsSortedByDistance()
    private val getRunsSortedByTimeInMillis=mainRepository.getAllRunsSortedByTimeInMillis()
    private val getRunsSortedByAvgSpeed=mainRepository.getAllRunsSortedByAvgSpeed()
    private val getRunsSortedByCaloriesBurned=mainRepository.getAllRunsSortedByCaloriesBurned()

    val runs=MediatorLiveData<List<Run>>()
    var sortType=SortType.DATE

    init {
        runs.addSource(getRunsSortedByDate){result->
            if(sortType==SortType.DATE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(getRunsSortedByDistance){result->
            if(sortType==SortType.DISTANCE){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(getRunsSortedByTimeInMillis){result->
            if(sortType==SortType.RUNNING_TIME){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(getRunsSortedByAvgSpeed){result->
            if(sortType==SortType.AVG_SPEED){
                result?.let { runs.value=it }
            }
        }
        runs.addSource(getRunsSortedByCaloriesBurned){result->
            if(sortType==SortType.CALORIES_BURNED){
                result?.let { runs.value=it }
            }
        }
    }

    fun sortRuns(sortType: SortType)=when(sortType){
        SortType.DATE-> getRunsSortedByDate.value?.let { runs.value=it }
        SortType.RUNNING_TIME-> getRunsSortedByTimeInMillis.value?.let { runs.value=it }
        SortType.DISTANCE-> getRunsSortedByDistance.value?.let { runs.value=it }
        SortType.AVG_SPEED-> getRunsSortedByAvgSpeed.value?.let { runs.value=it }
        SortType.CALORIES_BURNED-> getRunsSortedByCaloriesBurned.value?.let { runs.value=it }
    }.also { this.sortType=sortType }

}