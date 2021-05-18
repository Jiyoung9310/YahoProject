package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.R
import com.android.yaho.di.ContextDelegate
import com.android.yaho.local.cache.MountainListCache
import com.android.yaho.local.db.RecordEntity
import com.android.yaho.millisecondsToHourTimeFormat
import com.android.yaho.repository.ClimbingRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch

class ClimbingDetailViewModel(private val contextDelegate: ContextDelegate,
                              private val repo: ClimbingRepository,
                              private val mountainListCache: MountainListCache,
) : ViewModel() {

    private val _climbingData = MutableLiveData<ClimbingDetailDataUseCase>()
    val climbingData : LiveData<ClimbingDetailDataUseCase> get() = _climbingData

    private val _noData = MutableLiveData<Unit>()
    val noData: LiveData<Unit> get() = _noData

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun getClimbingData(climbingId: String) {
        viewModelScope.launch {
            repo.getClimbingData(climbingId)
                .catch { e: Throwable -> _error.value = e }
                .collect {
                    if(it == null){
                        _noData.value = Unit
                    } else {
                        _climbingData.value = it.toUseCase().apply {
                            mountainAddress = mountainListCache.getAddress(it.mountainId)
                        }
                    }
                }
        }
    }

    private fun RecordEntity.toUseCase() : ClimbingDetailDataUseCase = ClimbingDetailDataUseCase(
            climbingDate = climbingDate,
            mountainId = mountainId,
            mountainNameTitle = contextDelegate.getString(R.string.climbing_detail_mountain_name, mountainName, mountainVisitCount),
            allRunningTime = allRunningTime.millisecondsToHourTimeFormat(),
            totalClimbingTime = totalClimbingTime.millisecondsToHourTimeFormat(),
            restTime = (allRunningTime - totalClimbingTime).millisecondsToHourTimeFormat(),
            totalDistance = contextDelegate.getString(R.string.kilo_meter_unit, totalDistance),
            averageSpeed = contextDelegate.getString(R.string.speed_unit, averageSpeed.toFloat()),
            maxSpeed = contextDelegate.getString(R.string.speed_unit, maxSpeed),
            startHeight = contextDelegate.getString(R.string.meter_unit, startHeight),
            maxHeight = contextDelegate.getString(R.string.meter_unit, maxHeight)
        )

}

data class ClimbingDetailDataUseCase(
    val climbingDate : String,
    val mountainId : Int,
    val mountainNameTitle : String,
    var mountainAddress : String = "",
    val allRunningTime : String,
    val totalClimbingTime : String,
    val restTime : String,
    val totalDistance : String,
    val averageSpeed : String,
    val maxSpeed : String,
    val startHeight : String,
    val maxHeight : String,
)