package com.android.yaho.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.repository.ClimbingRepository
import kotlinx.coroutines.launch

class ClimbingDetailViewModel(private val repo: ClimbingRepository) : ViewModel() {

    fun getClimbingData(climbingId: String) {
        viewModelScope.launch {

        }
    }
}