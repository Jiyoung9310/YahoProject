package com.android.yaho.viewmodel

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.MountainData
import com.android.yaho.di.ContextDelegate
import com.android.yaho.repository.MountainRepository
import com.android.yaho.screen.ReadyActivity
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlin.math.abs

class ReadyViewModel(
    private val contextDelegate: ContextDelegate,
    private val mountainRepo: MountainRepository
) : ViewModel() {

    private val _moveScreen = MutableLiveData<Pair<String, Bundle?>>()
    val moveScreen: LiveData<Pair<String, Bundle?>> get() = _moveScreen

    private val _showLoading = MutableLiveData<Boolean>()
    val showLoading: LiveData<Boolean> get() = _showLoading

    private val _checkPermissions = MutableLiveData<Boolean>()
    val checkPermissions: LiveData<Boolean> get() = _checkPermissions

    private val _clickNearMountainLocation = MutableLiveData<Unit>()
    val clickNearMountainLocation: LiveData<Unit> get() = _clickNearMountainLocation

    private val _clickMyCurrentLocation = MutableLiveData<Unit>()
    val clickMyCurrentLocation: LiveData<Unit> get() = _clickMyCurrentLocation

    private val _clickMountain = MutableLiveData<MountainData>()
    val clickMountain: LiveData<MountainData> get() = _clickMountain

    private val _nearByList = MutableLiveData<List<MountainData>>()
    val nearByList: LiveData<List<MountainData>> get() = _nearByList

    private val _myLocation = MutableLiveData<Location>()
    val myLocation: LiveData<Location> get() = _myLocation

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun moveScreen(screen: String, bundle: Bundle? = null) {
        _moveScreen.value = screen to bundle
    }

    fun onClickMyCurrentLocation() {
        _clickMyCurrentLocation.value = Unit
    }

    fun checkPermissions() {
        _checkPermissions.value = ReadyActivity.PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                contextDelegate.getContext(),
                it
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

    fun getMyLocation(location: Location) {
        _myLocation.value = location
    }

    fun getNearMountain(location: Location) {
        viewModelScope.launch {
            _showLoading.value = true
            mountainRepo.getMountainList()
                .catch { e: Throwable -> _error.value = e }
                .onCompletion {
                    _showLoading.value = false
                }
                .collect { list ->
                    val data = list.filter {
                        abs(it.latitude - location.latitude) < 0.1
                    }.filter {
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