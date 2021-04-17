package com.android.yaho.viewmodel

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.MountainData
import com.android.yaho.repository.MountainRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class MainViewModel(private val mountainRepo: MountainRepository) : ViewModel() {

    private val _nearByList = MutableLiveData<List<MountainData>>()
    val nearByList : LiveData<List<MountainData>> get() = _nearByList

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun startTracking() {

    }

    fun getNearByMountain(location: Location) {
        viewModelScope.launch {
            mountainRepo.getNearBy(location.latitude, location.longitude)
                .catch { e:Throwable -> _error.value = e }
                .collect{
                    _nearByList.value = it
                }
        }
    }
}