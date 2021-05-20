package com.android.yaho.viewmodel

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

    private val _recordList = MutableLiveData<List<UIItem>>()
    val recordList : LiveData<List<UIItem>> get() = _recordList

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
                    val recordItemList = mutableListOf<UIItem>()
                    it.map { data ->
                        UIItem(
                            id = data.recordId,
                            item = RecordUseCase(
                                recordId = data.recordId,
                                headerDate = data.recordId.convertHeaderDateFormat(),
                                recordDate = data.recordId.convertRecordDateFormat(),
                                mountainName = data.mountainName,
                                runningTime = data.allRunningTime.millisecondsToHourTimeFormat(),
                                distance = (data.totalDistance / 1000).km(contextDelegate.getContext()),
                            ),
                            viewType = RecordListAdapter.RECORD_ITEM_VIEW_TYPE
                        )
                    }.apply {
                        groupBy { item -> (item.item as RecordUseCase).headerDate }
                            .forEach { (header, item) ->
                                recordItemList.add(UIItem(
                                    item = RecordHeader(header),
                                    viewType = RecordListAdapter.RECORD_HEADER_VIEW_TYPE)
                                )
                                recordItemList.addAll(item)
                            }
                        _recordList.value = recordItemList
                    }

                }
        }
    }

}

data class RecordHeader(
    val headerDate: String
)

data class RecordUseCase(
    val recordId : String,
    val headerDate : String,
    val recordDate : String,
    val mountainName : String,
    val runningTime : String,
    val distance : String,
)