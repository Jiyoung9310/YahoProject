package com.climbing.yaho.viewmodel

import androidx.lifecycle.*
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.repository.ClimbingRepository
import com.climbing.yaho.repository.ClimbingResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClimbingDoneViewModel @Inject constructor(
    private val repo: ClimbingRepository,
    private val climbingCache: LiveClimbingCache,
    private val pref: YahoPreference,
) : ViewModel() {

    private val _saveResult = MutableLiveData<Boolean>()
    val saveResult: LiveData<Boolean> get() = _saveResult
    private val _animationResult = MutableLiveData<Boolean>()

    private val _goToDetail = MediatorLiveData<String>().apply {
        addSource(_saveResult) {
            if (it && _animationResult.value == true) this.value = climbingId
        }
        addSource(_animationResult) {
            if (_saveResult.value == true && it) this.value = climbingId
        }
    }
    val goToDetail: LiveData<String> get() = _goToDetail

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    private val climbingId = System.currentTimeMillis().toString()

    fun saveClimbData() {
        // TODO : network 연결 아닌 상태인 케이스 처리 추가 필요
        viewModelScope.launch {
            repo.postClimbingData(climbingId)
                .catch { e: Throwable -> _error.value = e }
                .collect { data ->
                    if (data == ClimbingResult.Success) updateVisitMountain()
                }
        }
    }

    private fun updateVisitMountain() {
        viewModelScope.launch {
            repo.updateVisitMountain()
                .catch { e: Throwable -> _error.value = e }
                .collect {
                    _saveResult.value = it == ClimbingResult.Success
                    climbingCache.clearCache()
                    pref.clearSelectedMountain()
                }
        }
    }

    fun animationEnd() {
        _animationResult.value = true
    }
}