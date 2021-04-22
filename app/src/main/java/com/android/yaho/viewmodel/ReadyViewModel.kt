package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.yaho.repository.MountainRepository

class ReadyViewModel(private val mountainRepo: MountainRepository) : ViewModel() {

    private val _moveScreen = MutableLiveData<String>()
    val moveScreen : LiveData<String> get() = _moveScreen

    fun moveScreen(screen: String) {
        _moveScreen.value = screen
    }

}