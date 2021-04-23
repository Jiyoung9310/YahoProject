package com.android.yaho.viewmodel

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.MountainData
import com.android.yaho.repository.MountainRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.math.abs

class ReadyViewModel(private val mountainRepo: MountainRepository) : ViewModel() {

    private val _moveScreen = MutableLiveData<Pair<String, Bundle?>>()
    val moveScreen : LiveData<Pair<String, Bundle?>> get() = _moveScreen

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading : LiveData<Boolean> get() = _showLoading

    private val _clickLocation = MutableLiveData<Unit>()
    val clickLocation : LiveData<Unit> get() = _clickLocation

    private val _clickMountain = MutableLiveData<MountainData>()
    val clickMountain : LiveData<MountainData> get() = _clickMountain

    private val _nearByList = MutableLiveData<List<MountainData>>()
    val nearByList : LiveData<List<MountainData>> get() = _nearByList

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    private var _currentLocation: Location? = null
    val currentLocation: Location? get() = _currentLocation

    fun moveScreen(screen: String, bundle: Bundle? = null) {
        _moveScreen.value = screen to bundle
    }

    fun onClickLocation() {
        _clickLocation.value = Unit
    }

    fun getNearMountain(location: Location) {
        _currentLocation = location
        viewModelScope.launch {
            _showLoading.value = true
            mountainRepo.getMountainList()
                .catch { e: Throwable -> _error.value = e }
                .onCompletion {
                    _showLoading.value = false
                }
                .collect{ list ->
                    val data = list.filter {
                        abs(it.latitude - location.latitude) < 0.1
                    }.filter{
                        abs(it.longitude - location.longitude) < 0.1
                    }.take(4)
                    Log.d("MainViewModel", "getNearByMountain $data")
                    _nearByList.value = data
                }
        }
    }

    fun onClickMountain(mountain: MountainData) {
        _clickMountain.value = mountain
    }

}