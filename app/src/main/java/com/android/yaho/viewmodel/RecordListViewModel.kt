package com.android.yaho.viewmodel

import android.icu.text.AlphabeticIndex
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.*
import com.android.yaho.di.ContextDelegate
import com.android.yaho.repository.ClimbingRepository
import com.android.yaho.ui.RecordListAdapter
import com.android.yaho.ui.UIItem
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecordListViewModel(private val contextDelegate: ContextDelegate,
                          private val repo: ClimbingRepository) : ViewModel() {

    private val _recordList = MutableLiveData<List<RecordItem>>()
    val recordList : LiveData<List<RecordItem>> get() = _recordList

    private val _recordHeaderDateList = MutableLiveData<Array<String>>()
    val recordHeaderDateList : LiveData<Array<String>> get() = _recordHeaderDateList

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    init {
        getRecordList()
    }

    fun getRecordList() {
        viewModelScope.launch {
            repo.getClimbingRecordList()
                .catch { e: Throwable -> _error.value = e }
                .collect {
                    val recordItemList = mutableListOf<RecordItem>()
                    val recordHeaderList = mutableListOf<String>()
                    recordItemList.add(RecordItem.RecordTitle(contextDelegate.getString(R.string.climbing_record_select_all)))
                    recordHeaderList.add(contextDelegate.getString(R.string.climbing_record_select_all))

                    it.sortedByDescending { it.recordId.toLong() }
                        .map { data ->
                            RecordItem.RecordUseCase(
                                recordId = data.recordId,
                                headerDate = data.recordId.convertHeaderDateFormat(),
                                recordDate = data.recordId.convertRecordDateFormat(),
                                mountainName = data.mountainName,
                                runningTime = data.allRunningTime.millisecondsToHourTimeFormat(),
                                distance = (data.totalDistance / 1000).km(contextDelegate.getContext()),
                            )
                        }.apply {
                            groupBy { item -> item.headerDate }
                                .forEach { (header, item) ->
                                    recordHeaderList.add(header)
                                    recordItemList.add(RecordItem.RecordHeader(header))
                                    recordItemList.addAll(item)
                                }
                            _recordList.value = recordItemList
                            _recordHeaderDateList.value = recordHeaderList.toTypedArray()
                        }
                }
        }
    }

    fun onSelectDate(index: Int) {
        val recordDate = recordHeaderDateList.value?.get(index)
        recordDate?.let {
            val newList = mutableListOf<RecordItem>()
            recordList.value?.let {
                it.filter { it.headerDate == recordDate }
                    .apply {
                        newList.add(RecordItem.RecordTitle(selectDate = recordDate))
                        newList.addAll(this)
                    }
                _recordList.value = newList
            }
        }
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
        val recordId : String,
        override val headerDate : String,
        val recordDate : String,
        val mountainName : String,
        val runningTime : String,
        val distance : String,
    ) : RecordItem(headerDate)
}