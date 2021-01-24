package com.udacity.asteroidradar.work

import android.app.Application
import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.Repository
import com.udacity.asteroidradar.database.getInstance
import com.udacity.asteroidradar.main.MainViewModel
import retrofit2.HttpException

class RefreshAsteroidsWork(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "RefreshAsteroidWork"
    }

    override suspend fun doWork(): Result {
        val database = getInstance(applicationContext)
        val repository = Repository(database, MainViewModel(applicationContext as Application))

        return try {
            repository.refreshAsteroids()
            Result.success()
        }catch (e: HttpException){
            Result.failure()
        }
    }
}