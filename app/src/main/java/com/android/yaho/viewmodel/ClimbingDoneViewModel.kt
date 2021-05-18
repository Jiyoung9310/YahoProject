package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.repository.ClimbingRepository
import com.android.yaho.repository.ClimbingResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

class ClimbingDoneViewModel(private val repo: ClimbingRepository,
                            private val climbingCache: LiveClimbingCache,
                            private val pref : YahoPreference,
) : ViewModel() {

    private val _saveResult = MutableLiveData<String>()
    val saveResult : LiveData<String> get() = _saveResult

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    private val climbingId = System.currentTimeMillis().toString()
    init {
        saveClimbData()
    }

    fun saveClimbData() {
        // TODO : network 연결 아닌 상태인 케이스 처리 추가 필요
        viewModelScope.launch {
            repo.postClimbingData(climbingId)
                .catch { e:Throwable -> _error.value = e }
                .collect { data ->
                    if(data == ClimbingResult.Success) updateVisitMountain()
                }
        }
    }

    private fun updateVisitMountain() {
        viewModelScope.launch {
            repo.updateVisitMountain()
                .catch { e:Throwable -> _error.value = e }
                .collect {
                    _saveResult.value = climbingId
                    climbingCache.clearCache()
                    pref.clearSelectedMountain()
                }
        }
    }
}