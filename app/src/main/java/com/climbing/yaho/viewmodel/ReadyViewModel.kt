package com.climbing.yaho.viewmodel

import android.location.Location
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.data.MountainData
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.local.cache.MountainListCache
import com.climbing.yaho.repository.ClimbingRepository
import com.climbing.yaho.screen.ClimbingActivity
import com.climbing.yaho.screen.ReadyActivity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class ReadyViewModel @Inject constructor(
    private val contextDelegate: ContextDelegate,
    private val mountainCache: MountainListCache,
    private val repo: ClimbingRepository,
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

    private val _countDownNumber = MutableLiveData<Int>()
    val countDownNumber: LiveData<Int> get() = _countDownNumber

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    var selectedMountain : MountainData? = null
    var visitCount : Int = 0

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
        val data = mountainCache.data.map { it.value }
            .filter {
                abs(it.latitude - location.latitude) < 0.1
            }.filter {
                abs(it.longitude - location.longitude) < 0.1
            }.take(4)
        Log.d("ReadyViewModel", "getNearByMountain $data")
        _nearByList.value = data
    }

    fun countDown() {
        viewModelScope.launch {
            val totalSeconds = TimeUnit.SECONDS.toSeconds(3)
            for (second in totalSeconds downTo 1) {
                _countDownNumber.value = second.toInt()
                delay(1000)
            }
            moveScreen(
                ReadyActivity.SCREEN_GO_CLIMBING,
                Bundle().apply {
                    putParcelable(ClimbingActivity.KEY_MOUNTAIN_DATA, selectedMountain)
                    putInt(ClimbingActivity.KEY_MOUNTAIN_VISIT_COUNT, visitCount)
                })
        }
    }

    fun onClickMountain(mountain: MountainData) {
        selectedMountain = mountain
        viewModelScope.launch {
            repo.getVisitMountain(mountain.id)
                .catch { e:Throwable -> _error.value = e }
                .collect { count ->
                    visitCount = count + 1
                    _clickMountain.value = mountain
                }
        }
    }

}