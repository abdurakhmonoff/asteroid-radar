package com.udacity.asteroidradar

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.api.asDatabaseAsteroid
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.main.AsteroidStatus
import com.udacity.asteroidradar.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.*

class Repository(private val database: AsteroidDatabase, private val viewModel: MainViewModel) {
    private val startDate = Calendar.getInstance()
    private val endDate = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR,7) }

    val allAsteroids:LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAllAsteroids()){
        it.asDomainModel()
    }

    val todayAsteroids:LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getToday(SimpleDateFormat("yyyy-MM-dd",
        Locale.US).format(startDate.time))){
        it.asDomainModel()
    }

    val weekAsteroids:LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getWeek(SimpleDateFormat("yyyy-MM-dd",Locale.US).format(startDate.time),SimpleDateFormat("yyyy-MM-dd",
        Locale.US).format(endDate.time))){
        it.asDomainModel()
    }

    suspend fun refreshAsteroids() {
        withContext(Dispatchers.IO) {
            viewModel.changeStatus(AsteroidStatus.LOADING)
            try {
                val today = Calendar.getInstance()
                val afterSevenDays = Calendar.getInstance().also { it.add(Calendar.DAY_OF_YEAR,7) }
                val asteroidsJson = AsteroidApi.retrofitService.getJsonObj(
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(today.timeInMillis)),
                    SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(afterSevenDays.timeInMillis)),
                    API_KEY
                ).await()
                val allAsteroids = parseAsteroidsJsonResult(JSONObject(asteroidsJson))

                database.asteroidDao.insertAll(*allAsteroids.asDatabaseAsteroid())

                viewModel.changeStatus(AsteroidStatus.DONE)
            } catch (e: Exception) {
                viewModel.changeStatus(AsteroidStatus.ERROR)
            }
        }
    }

}