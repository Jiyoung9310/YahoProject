package com.climbing.yaho.viewmodel


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.repository.LoginRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class IntroViewModel(private val repo : LoginRepository) : ViewModel() {
    private val _goToHome = MutableLiveData<Unit>()
    val goToHome: LiveData<Unit> get() = _goToHome

    private val _goToLogin = MutableLiveData<Unit>()
    val goToLogin: LiveData<Unit> get() = _goToLogin

    fun startIDCheck() {
        viewModelScope.launch {
            val totalSeconds = TimeUnit.SECONDS.toSeconds(3)
            for (second in totalSeconds downTo 1) {
                delay(1000)
            }
            if(repo.getUserID() == null) {
                _goToLogin.value = Unit
            } else {
                _goToHome.value = Unit
            }
        }
    }

}