package com.android.yaho.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.MountainData
import com.android.yaho.repository.MountainRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.abs

class MainViewModel(private val mountainRepo: MountainRepository) : ViewModel() {

    private val _nearByList = MutableLiveData<List<MountainData>>()
    val nearByList : LiveData<List<MountainData>> get() = _nearByList

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun startTracking() {

    }

    fun getNearByMountain(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            mountainRepo.getMountainList()
                .catch { e:Throwable -> _error.value = e }
                .collect{ list ->
                    val data = list.filter {
                        abs(it.latitude - latitude) < 0.1
                    }.filter{
                        abs(it.longitude - longitude) < 0.1
                    }.take(4)
                    Log.d("MainViewModel", "getNearByMountain $data")
                    _nearByList.value = data
                }
        }
    }
}