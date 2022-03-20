package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.data.UserClimbingData
import com.climbing.yaho.repository.UserDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: UserDataRepository
) : ViewModel() {

    private val _userData = MutableLiveData<UserClimbingData>()
    val userData : LiveData<UserClimbingData> get() = _userData

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun getUserData() {
        viewModelScope.launch {
            repo.getUserData()
                .catch { e:Throwable -> _error.value = e }
                .collect { data ->
                    _userData.value = data
                }
        }
    }
}