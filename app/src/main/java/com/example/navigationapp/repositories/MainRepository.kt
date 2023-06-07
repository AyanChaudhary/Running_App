package com.example.navigationapp.repositories

import com.example.navigationapp.Model.Run
import com.example.navigationapp.db.RunningDao
import javax.inject.Inject

class MainRepository @Inject constructor(val runningDao: RunningDao) {

    suspend fun insertRun(run:Run)=runningDao.insertRun(run)

    suspend fun deleteRun(run: Run)=runningDao.deleteRun(run)

    fun getAllRunsSortedByDate()=runningDao.getAllRunsSortedByDate()

    fun getAllRunsSortedByTimeInMillis()=runningDao.getAllRunsSortedByTimeInMillis()

    fun getAllRunsSortedByDistance()=runningDao.getAllRunsSortedByDistance()

    fun getAllRunsSortedByAvgSpeed()=runningDao.getAllRunsSortedByAvgSpeed()

    fun getAllRunsSortedByCaloriesBurned()=runningDao.getAllRunsSortedByCaloriesBurned()

    fun getTotalDistance()=runningDao.getTotalDistanceCovered()

    fun getTotalAvgSpeed()=runningDao.getAvgSpeed()

    fun getTotalCaloriesBurned()=runningDao.getTotalCaloriesBurned()

    fun getTotalTimeInMillis()=runningDao.getTotalTimeInMillis()
}