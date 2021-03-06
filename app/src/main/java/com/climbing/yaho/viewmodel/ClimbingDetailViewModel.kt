package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.R
import com.climbing.yaho.convertHourTimeFormat
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.local.cache.MountainListCache
import com.climbing.yaho.local.db.RecordEntity
import com.climbing.yaho.meter
import com.climbing.yaho.millisecondsToHourTimeFormat
import com.climbing.yaho.repository.ClimbingRepository
import com.climbing.yaho.repository.ClimbingResult
import com.naver.maps.geometry.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClimbingDetailViewModel @Inject constructor(
    private val contextDelegate: ContextDelegate,
    private val repo: ClimbingRepository,
    private val mountainListCache: MountainListCache,
) : ViewModel() {

    private val _climbingData = MutableLiveData<ClimbingDetailDataUseCase>()
    val climbingData: LiveData<ClimbingDetailDataUseCase> get() = _climbingData

    private val _sectionData = MutableLiveData<List<ClimbingDetailSectionUseCase>>()
    val sectionData: LiveData<List<ClimbingDetailSectionUseCase>> get() = _sectionData

    private val _pathData = MutableLiveData<List<LatLng>>()
    val pathData: LiveData<List<LatLng>> get() = _pathData

    private val _sectionMark = MutableLiveData<List<LatLng>>()
    val sectionMark: LiveData<List<LatLng>> get() = _sectionMark

    private val _deleteDone = MutableLiveData<Boolean>()
    val deleteDone: LiveData<Boolean> get() = _deleteDone

    private val _noData = MutableLiveData<Unit>()
    val noData: LiveData<Unit> get() = _noData

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    private var recordId: String = ""

    fun getClimbingData(climbingId: String) {
        recordId = climbingId
        viewModelScope.launch {
            repo.getClimbingData(climbingId)
                .catch { e: Throwable -> _error.value = e }
                .collect { record ->
                    if (record == null) {
                        _noData.value = Unit
                    } else {
                        _climbingData.value = record.toUseCase().apply {
                            mountainAddress = mountainListCache.getAddress(record.mountainId)
                        }

                        _pathData.value =
                            record.points?.map { LatLng(it.latitude, it.longitude) }.apply {
                                mutableListOf(this)
                            }

                        val sectionList = mutableListOf<ClimbingDetailSectionUseCase>()

                        record.sections?.let { sections ->
                            sectionList.add(
                                ClimbingDetailSectionUseCase(
                                    sectionNumber = 1,
                                    sectionTitle = contextDelegate.getString(R.string.climbing_detail_info_section_start_time_title),
                                    sectionPeriod = record.points?.get(0)?.timestamp?.let {
                                        convertHourTimeFormat(
                                            it
                                        )
                                    } ?: "",
                                )
                            )
                            for (i in 1 until sections.size - 1) {
                                sectionList.add(
                                    ClimbingDetailSectionUseCase(
                                        sectionNumber = i + 1,
                                        sectionTitle = contextDelegate.getString(R.string.climbing_detail_info_section_rest_title),
                                        sectionPeriod = run {
                                            val startRest =
                                                record.points?.get(sections[i].restIndex)?.timestamp?.let {
                                                    convertHourTimeFormat(it)
                                                } ?: ""
                                            val endRest =
                                                record.points?.get(sections[i].restIndex + 1)?.timestamp?.let {
                                                    convertHourTimeFormat(it)
                                                } ?: ""
                                            "$startRest ~ $endRest"
                                        },
                                        sectionData = ClimbingSectionData(
                                            climbingTime = sections[i].runningTime.millisecondsToHourTimeFormat(),
                                            distance = sections[i].distance.meter(contextDelegate.getContext()),
                                            calories = contextDelegate.getString(
                                                R.string.kcal_unit,
                                                sections[i].calories
                                            )
                                        )
                                    )
                                )
                            }
                            sectionList.add(ClimbingDetailSectionUseCase(
                                sectionNumber = sections.size,
                                sectionTitle = contextDelegate.getString(R.string.climbing_detail_info_section_end_time_title),
                                sectionPeriod = record.points?.last()?.timestamp?.let {
                                    convertHourTimeFormat(
                                        it
                                    )
                                } ?: "",
                                sectionData = ClimbingSectionData(
                                    climbingTime = sections.last().runningTime.millisecondsToHourTimeFormat(),
                                    distance = sections.last().distance.meter(contextDelegate.getContext()),
                                    calories = contextDelegate.getString(
                                        R.string.kcal_unit,
                                        sections.last().calories
                                    )
                                )
                            ))
                            _sectionData.value = sectionList


                            _sectionMark.value = record.points?.let { points ->
                                sections.map {
                                    points[it.restIndex]
                                }.map {
                                    LatLng(it.latitude, it.longitude)
                                }.apply {
                                    mutableListOf(this)
                                }
                            }
                        }

                    }
                }
        }
    }

    private fun RecordEntity.toUseCase(): ClimbingDetailDataUseCase =
        ClimbingDetailDataUseCase(
            climbingDate = climbingDate,
            mountainId = mountainId,
            mountainNameTitle = contextDelegate.getString(
                R.string.climbing_detail_mountain_name,
                mountainName,
                mountainVisitCount
            ),
            allRunningTime = allRunningTime.millisecondsToHourTimeFormat(),
            totalClimbingTime = totalClimbingTime.millisecondsToHourTimeFormat(),
            restTime = (allRunningTime - totalClimbingTime).millisecondsToHourTimeFormat(),
            totalDistance = totalDistance.meter(contextDelegate.getContext()),
            averageSpeed = contextDelegate.getString(R.string.speed_unit, averageSpeed.toFloat()),
            maxSpeed = contextDelegate.getString(R.string.speed_unit, maxSpeed),
            startHeight = contextDelegate.getString(R.string.meter_unit, startHeight),
            maxHeight = contextDelegate.getString(R.string.meter_unit, maxHeight)
        )

    fun deleteRecord() {
        viewModelScope.launch {
            repo.deleteClimbingData(recordId)
                .catch { e: Throwable ->
                    _error.value = e
                }
                .collect {
                    _deleteDone.value = it == ClimbingResult.Success
                }
        }
    }
}

data class ClimbingDetailDataUseCase(
    val climbingDate: String,
    val mountainId: Int,
    val mountainNameTitle: String,
    var mountainAddress: String = "",
    val allRunningTime: String,
    val totalClimbingTime: String,
    val restTime: String,
    val totalDistance: String,
    val averageSpeed: String,
    val maxSpeed: String,
    val startHeight: String,
    val maxHeight: String,
)

data class ClimbingDetailSectionUseCase(
    val sectionNumber: Int,
    val sectionTitle: String,
    val sectionPeriod: String,
    val sectionData: ClimbingSectionData? = null
)

data class ClimbingSectionData(
    val climbingTime: String,
    val distance: String,
    val calories: String
)