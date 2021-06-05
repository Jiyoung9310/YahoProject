package com.climbing.yaho.viewmodel


import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.di.ContextDelegate
import com.climbing.yaho.repository.LoginRepository
import com.climbing.yaho.screen.ReadyActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class IntroViewModel(private val contextDelegate: ContextDelegate,
                     private val repo : LoginRepository) : ViewModel() {
    private val _goToHome = MutableLiveData<Unit>()
    val goToHome: LiveData<Unit> get() = _goToHome

    private val _goToLogin = MutableLiveData<Unit>()
    val goToLogin: LiveData<Unit> get() = _goToLogin

    private val _checkPermissions = MutableLiveData<Boolean>()
    val checkPermissions: LiveData<Boolean> get() = _checkPermissions

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

    fun checkPermissions() {
        _checkPermissions.value = ReadyActivity.PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                contextDelegate.getContext(),
                it
            ) == PermissionChecker.PERMISSION_GRANTED
        }
    }

}