package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.*
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.repository.ClimbingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecordListViewModel @Inject constructor(
    private val contextDelegate: ContextDelegate,
    private val repo: ClimbingRepository
) : ViewModel() {

    private val _recordList = MutableLiveData<List<RecordItem>>()
    val recordList: LiveData<List<RecordItem>> get() = _recordList

    private val _recordHeaderDateList = MutableLiveData<Array<String>>()
    val recordHeaderDateList: LiveData<Array<String>> get() = _recordHeaderDateList

    private val _goToRecordDetail = MutableLiveData<Pair<String, String>>()
    val goToRecordDetail : LiveData<Pair<String, String>> get() = _goToRecordDetail

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    private val _allRecordList = mutableListOf<RecordItem>()

    init {
        getRecordList()
    }

    fun getRecordList() {
        viewModelScope.launch {
            repo.getClimbingRecordList()
                .catch { e: Throwable -> _error.value = e }
                .collect {
                    val recordHeaderList = mutableListOf<String>()
                    _allRecordList.add(RecordItem.RecordTitle(contextDelegate.getString(R.string.climbing_record_select_all)))
                    recordHeaderList.add(contextDelegate.getString(R.string.climbing_record_select_all))

                    it.sortedByDescending { it.recordId.toLong() }
                        .map { data ->
                            RecordItem.RecordUseCase(
                                recordId = data.recordId,
                                headerDate = data.recordId.convertHeaderDateFormat(),
                                recordDate = data.recordId.convertRecordDateFormat(),
                                mountainName = data.mountainName,
                                runningTime = data.allRunningTime.millisecondsToHourTimeFormat(),
                                distance = (data.totalDistance / 1000).meter(contextDelegate.getContext()),
                            )
                        }.apply {
                            groupBy { item -> item.headerDate }
                                .forEach { (header, item) ->
                                    recordHeaderList.add(header)
                                    _allRecordList.add(RecordItem.RecordHeader(header))
                                    _allRecordList.addAll(item)
                                }
                            _recordList.value = _allRecordList
                            _recordHeaderDateList.value = recordHeaderList.toTypedArray()
                        }
                }
        }
    }

    fun onSelectDate(index: Int) {
        val recordDate = recordHeaderDateList.value?.get(index)
        recordDate?.let {
            val newList = mutableListOf<RecordItem>()
            if (index == 0) {
                newList.addAll(_allRecordList)
            } else {
                _allRecordList.filter { it.headerDate == recordDate }
                    .apply {
                        newList.add(RecordItem.RecordTitle(selectDate = recordDate))
                        newList.addAll(this)
                    }
            }
            _recordList.value = newList
        }
    }

    fun onClickRecordDetail(id: String, name: String) {
        _goToRecordDetail.value = id to name
    }
}

sealed class RecordItem(open val headerDate: String) {
    data class RecordTitle(
        val selectDate: String
    ) : RecordItem("")

    data class RecordHeader(
        override val headerDate: String
    ) : RecordItem(headerDate)

    data class RecordUseCase(
        val recordId: String,
        override val headerDate: String,
        val recordDate: String,
        val mountainName: String,
        val runningTime: String,
        val distance: String,
    ) : RecordItem(headerDate)
}