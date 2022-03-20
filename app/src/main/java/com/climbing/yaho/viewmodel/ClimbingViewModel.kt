package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.local.db.PointEntity
import com.climbing.yaho.local.db.RecordEntity
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.ticker
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClimbingViewModel @Inject constructor(
    private val climbingCache: LiveClimbingCache
) : ViewModel() {

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

    private val _stampMarker = MutableLiveData<LatLng>()
    val stampMarker: LiveData<LatLng> get() = _stampMarker

    private val _climbingData = MutableLiveData<ClimbingDetailUseCase>()
    val climbingData: LiveData<ClimbingDetailUseCase> get() = _climbingData

    private val _runningTime = MutableLiveData<Long>()
    val runningTime: LiveData<Long> get() = _runningTime

    private val _clickDone = MutableLiveData<RecordEntity?>()
    val clickDone: LiveData<RecordEntity?> get() = _clickDone

    fun setRunningTime(runningCount: Long, restingCount: Long, term: Long, isActive: Boolean) {
        activeCount = runningCount
        restCount = restingCount
        count = if(isActive) {
            activeCount + term
        } else {
            restCount + term
        }
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
            _stampMarker.value = climbingCache.latlngPaths.last()
        } else {
            activeCount = count
            count = restCount
            climbingCache.updateSection()
            _stampMarker.value = climbingCache.latlngPaths.last()
        }
    }

    fun onClickDone() {
        _clickDone.value = climbingCache.done()
    }
}

data class ClimbingDetailUseCase(
    val height: Double = 0.0,
    val allDistance: Float = 0f,
)