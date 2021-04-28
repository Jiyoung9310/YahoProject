package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ClimbingViewModel : ViewModel() {
    private val _boundService = MutableLiveData<Boolean>()
    val boundService : LiveData<Boolean> get() = _boundService

    fun onSettingService(isBound : Boolean) {
        _boundService.value = isBound
    }
}