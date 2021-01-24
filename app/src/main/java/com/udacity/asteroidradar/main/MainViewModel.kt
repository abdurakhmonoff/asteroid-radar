package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.Repository
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.AsteroidApiFilter
import com.udacity.asteroidradar.database.getInstance
import kotlinx.coroutines.launch

enum class AsteroidStatus { LOADING, DONE, ERROR }

class MainViewModel(app: Application) : AndroidViewModel(app) {

    private val database = getInstance(app.applicationContext)
    private val repository = Repository(database, this)

    private val _status = MutableLiveData<AsteroidStatus>()
    val status: LiveData<AsteroidStatus>
        get() = _status

    private val _image = MutableLiveData<PictureOfDay>()
    val image: LiveData<PictureOfDay>
        get() = _image

    private val _navigateToDetail = MutableLiveData<Asteroid>()
    val navigateToDetail: LiveData<Asteroid>
        get() = _navigateToDetail

    private val asteroidFilterType = MutableLiveData<AsteroidApiFilter>()

    fun navigateToDetail(asteroid: Asteroid) {
        _navigateToDetail.value = asteroid
    }

    fun navigateToDetailCompleted() {
        _navigateToDetail.value = null
    }

    init {
        viewModelScope.launch {
            repository.refreshAsteroids()
            try {
                _image.value = AsteroidApi.retrofitService.getImageOfDay(API_KEY)
            } catch (e: Exception) {
            }
        }
        changeFilterType(AsteroidApiFilter.WEEK)
    }

    fun changeStatus(status: AsteroidStatus) {
        _status.postValue(status)
    }

    private var _asteroids = Transformations.switchMap<AsteroidApiFilter,List<Asteroid>>(asteroidFilterType){
        when(it){
            AsteroidApiFilter.WEEK -> repository.weekAsteroids
            AsteroidApiFilter.TODAY -> repository.todayAsteroids
            else -> repository.allAsteroids
        }
    }
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    fun changeFilterType(filter: AsteroidApiFilter){
        asteroidFilterType.value = filter
    }
}