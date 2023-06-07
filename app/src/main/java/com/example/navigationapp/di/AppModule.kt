package com.example.navigationapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.navigationapp.db.RunningDatabase
import com.example.navigationapp.other.Constants
import com.example.navigationapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.navigationapp.other.Constants.KEY_NAME
import com.example.navigationapp.other.Constants.KEY_WEIGHT
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideRunDatabase(
        @ApplicationContext app:Context
    )= Room.databaseBuilder(
        app,RunningDatabase::class.java,
        "running_db"
    ).build()

    @Singleton
    @Provides
    fun provideRunDao(db:RunningDatabase)=db.runningDao()

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app: Context)=app.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME,MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref:SharedPreferences)=sharedPref.getString(KEY_NAME,"")?:""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences)=sharedPref.getFloat(KEY_WEIGHT,80f)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPref: SharedPreferences)=sharedPref.getBoolean(
        KEY_FIRST_TIME_TOGGLE,true)
}