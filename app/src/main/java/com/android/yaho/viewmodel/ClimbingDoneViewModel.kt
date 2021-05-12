package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.data.UserClimbingData
import com.android.yaho.repository.ClimbingRepository
import com.android.yaho.repository.ClimbingResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ClimbingDoneViewModel(private val repo: ClimbingRepository) : ViewModel() {

    private val _saveResult = MutableLiveData<ClimbingResult>()
    val saveResult : LiveData<ClimbingResult> get() = _saveResult

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    init {
        saveClimbData()
    }

    fun saveClimbData() {
        viewModelScope.launch {
            repo.postClimbingData()
                .catch { e:Throwable -> _error.value = e }
                .collect { data ->
                    _saveResult.value = data
                }
        }

    }
}