package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.local.db.PointEntity
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

class ClimbingViewModel(private val climbingCache: LiveClimbingCache) : ViewModel() {

    private var activeCount: Long = 0
    private var restCount: Long = 0
    private var count: Long = activeCount

    init {
        viewModelScope.launch {
            ticker(1000).consumeAsFlow()
                .collect {
                    _runningTime.value = count++
                }
        }
    }

    private val _boundService = MutableLiveData<Boolean>()
    val boundService: LiveData<Boolean> get() = _boundService

    private val _updateMap = MutableLiveData<PointEntity>()
    val updateMap: LiveData<PointEntity> get() = _updateMap

    private val _climbingData = MutableLiveData<ClimbingDetailUseCase>()
    val climbingData: LiveData<ClimbingDetailUseCase> get() = _climbingData

    private val _runningTime = MutableLiveData<Long>()
    val runningTime: LiveData<Long> get() = _runningTime

    fun setRunningTime(time: Long) {
        activeCount = time
    }

    fun onSettingService(isBound: Boolean) {
        _boundService.value = isBound
    }

    fun updateCurrentLocation() {
        _updateMap.value = climbingCache.getLastClimbingData()
        _climbingData.value = ClimbingDetailUseCase(
            height = climbingCache.getLastClimbingData().altitude,
            allDistance = climbingCache.getRecord()?.totalDistance ?: 0f
        )
    }

    fun onClickPause(isActive: Boolean) {
        if(isActive) {
            restCount = count
            count = activeCount
        } else {
            activeCount = count
            count = restCount
            climbingCache.updateSection()
        }
    }
}

data class ClimbingDetailUseCase(
    val height: Double = 0.0,
    val allDistance: Float = 0f,
)