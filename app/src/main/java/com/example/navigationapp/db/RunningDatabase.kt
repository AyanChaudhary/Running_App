package com.example.navigationapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.navigationapp.Model.Run

@Database(entities = [Run::class], version = 1)
@TypeConverters(Convertors::class)
abstract class RunningDatabase : RoomDatabase(){
    abstract fun runningDao() : RunningDao
}