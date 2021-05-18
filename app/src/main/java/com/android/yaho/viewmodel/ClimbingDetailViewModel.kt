package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.local.cache.MountainListCache
import com.android.yaho.local.db.RecordEntity
import com.android.yaho.repository.ClimbingRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClimbingDetailViewModel(private val repo: ClimbingRepository,
                              private val mountainListCache: MountainListCache,
) : ViewModel() {

    private val _climbingData = MutableLiveData<RecordEntity?>()
    val climbingData : LiveData<RecordEntity?> get() = _climbingData

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun getClimbingData(climbingId: String) {
        viewModelScope.launch {
            repo.getClimbingData(climbingId)
                .catch { e: Throwable -> _error.value = e }
                .collect {
                    _climbingData.value = it
                }
        }
    }

}

data class ClimbingDetailDataUseCase(
    val climbingDate : String,
    val mountainId : Int,
    val mountainName : String,
    val mountainVisitCount : Int,
    val mountainAddress : String,

)