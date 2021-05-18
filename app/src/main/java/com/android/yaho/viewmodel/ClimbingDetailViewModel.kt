package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.R
import com.android.yaho.convertHourTimeFormat
import com.android.yaho.di.ContextDelegate
import com.android.yaho.local.cache.MountainListCache
import com.android.yaho.local.db.RecordEntity
import com.android.yaho.millisecondsToHourTimeFormat
import com.android.yaho.repository.ClimbingRepository
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClimbingDetailViewModel(private val contextDelegate: ContextDelegate,
                              private val repo: ClimbingRepository,
                              private val mountainListCache: MountainListCache,
) : ViewModel() {

    private val _climbingData = MutableLiveData<ClimbingDetailDataUseCase>()
    val climbingData : LiveData<ClimbingDetailDataUseCase> get() = _climbingData

    private val _sectionData = MutableLiveData<List<ClimbingDetailSectionUseCase>>()
    val sectionData : LiveData<List<ClimbingDetailSectionUseCase>> get() = _sectionData

    private val _noData = MutableLiveData<Unit>()
    val noData: LiveData<Unit> get() = _noData

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun getClimbingData(climbingId: String) {
        viewModelScope.launch {
            repo.getClimbingData(climbingId)
                .catch { e: Throwable -> _error.value = e }
                .collect { record ->
                    if(record == null){
                        _noData.value = Unit
                    } else {
                        _climbingData.value = record.toUseCase().apply {
                            mountainAddress = mountainListCache.getAddress(record.mountainId)
                        }

                        val sectionList = mutableListOf<ClimbingDetailSectionUseCase>()

                        record.sections?.mapIndexed { index, pathSectionEntity ->
                            ClimbingDetailSectionUseCase(
                                sectionNumber = index + 1,
                                sectionTitle = if(index == 0) {
                                    contextDelegate.getString(R.string.climbing_detail_info_section_start_time_title)
                                } else {
                                    contextDelegate.getString(R.string.climbing_detail_info_section_rest_title)
                                },
                                sectionPeriod = if(index == 0) {
                                    record.points?.get(0)?.timestamp?.let { convertHourTimeFormat(it) } ?: ""
                                } else {
                                    val startRest = record.points?.get(pathSectionEntity.restIndex)?.timestamp?.let { convertHourTimeFormat(it) } ?: ""
                                    val endRest = record.points?.get(pathSectionEntity.restIndex + 1)?.timestamp?.let { convertHourTimeFormat(it) } ?: ""
                                    "$startRest ~ $endRest"
                                },
                                sectionData = ClimbingSectionData(
                                    climbingTime = pathSectionEntity.runningTime.millisecondsToHourTimeFormat(),
                                    distance =  contextDelegate.getString(R.string.kilo_meter_unit, pathSectionEntity.distance),
                                    calories = contextDelegate.getString(R.string.kcal_unit, pathSectionEntity.calories)
                                )
                            )
                        }?.apply {
                            sectionList.addAll(this)
                            sectionList.add(
                                ClimbingDetailSectionUseCase(
                                    sectionNumber = this.count() + 1,
                                    sectionTitle = contextDelegate.getString(R.string.climbing_detail_info_section_end_time_title),
                                    sectionPeriod = record.points?.last()?.timestamp?.let { convertHourTimeFormat(it) } ?: ""
                                )
                            )

                            _sectionData.value = sectionList
                        }
                    }
                }
        }
    }

    private fun RecordEntity.toUseCase() : ClimbingDetailDataUseCase =
        ClimbingDetailDataUseCase(
            climbingDate = climbingDate,
            mountainId = mountainId,
            mountainNameTitle = contextDelegate.getString(R.string.climbing_detail_mountain_name, mountainName, mountainVisitCount),
            allRunningTime = allRunningTime.millisecondsToHourTimeFormat(),
            totalClimbingTime = totalClimbingTime.millisecondsToHourTimeFormat(),
            restTime = (allRunningTime - totalClimbingTime).millisecondsToHourTimeFormat(),
            totalDistance = contextDelegate.getString(R.string.kilo_meter_unit, totalDistance / 1000),
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

data class ClimbingDetailSectionUseCase(
    val sectionNumber : Int,
    val sectionTitle : String,
    val sectionPeriod : String,
    val sectionData : ClimbingSectionData? = null
)

data class ClimbingSectionData(
    val climbingTime : String,
    val distance : String,
    val calories : String
)