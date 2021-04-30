package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.yaho.data.ClimbingRecordData
import com.android.yaho.data.LiveClimbingData
import com.android.yaho.data.cache.LiveClimbingCache
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ClimbingViewModel(private val climbingCache: LiveClimbingCache) : ViewModel() {

    init {

    }

    private val _boundService = MutableLiveData<Boolean>()
    val boundService : LiveData<Boolean> get() = _boundService

    private val _updateMap = MutableLiveData<LiveClimbingData>()
    val updateMap : LiveData<LiveClimbingData> get() = _updateMap

    private val _climbingData = MutableLiveData<ClimbingRecordData>()
    val climbingData: LiveData<ClimbingRecordData> get() = _climbingData

    fun onSettingService(isBound : Boolean) {
        _boundService.value = isBound
    }

    fun updateCurrentLocation() {
        _updateMap.value = climbingCache.getLastClimbingData()
        _climbingData.value = climbingCache.getRecord()
    }
}