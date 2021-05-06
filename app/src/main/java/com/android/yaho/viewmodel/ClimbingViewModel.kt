package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.LiveClimbingData
import com.android.yaho.local.cache.LiveClimbingCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class ClimbingViewModel(private val climbingCache: LiveClimbingCache) : ViewModel() {

    private var count: Long = 0

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

    private val _updateMap = MutableLiveData<LiveClimbingData>()
    val updateMap: LiveData<LiveClimbingData> get() = _updateMap

    private val _climbingData = MutableLiveData<ClimbingDetailUseCase>()
    val climbingData: LiveData<ClimbingDetailUseCase> get() = _climbingData

    private val _runningTime = MutableLiveData<Long>()
    val runningTime: LiveData<Long> get() = _runningTime

    fun setRunningTime(time: Long) {
        count = time
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
}

data class ClimbingDetailUseCase(
    val height: Double = 0.0,
    val allDistance: Float = 0f,
)