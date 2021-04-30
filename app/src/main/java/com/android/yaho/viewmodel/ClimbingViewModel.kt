package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.yaho.data.ClimbingRecordData
import com.android.yaho.data.cache.LiveClimbingCache
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class ClimbingViewModel : ViewModel(), KoinComponent {

    init {

    }

    private val _boundService = MutableLiveData<Boolean>()
    val boundService : LiveData<Boolean> get() = _boundService

    private val _climbingData = MutableLiveData<ClimbingRecordData>()
    val climbingData: LiveData<ClimbingRecordData> get() = _climbingData

    fun onSettingService(isBound : Boolean) {
        _boundService.value = isBound
    }

    fun updateCurrentLocation() {
        _climbingData.value = get<LiveClimbingCache>().getRecord()
    }
}