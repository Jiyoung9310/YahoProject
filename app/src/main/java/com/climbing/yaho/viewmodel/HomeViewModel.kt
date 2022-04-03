package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.data.UserData
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.repository.UserDataRepository
import com.naver.maps.map.e
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: UserDataRepository,
    private val preference: YahoPreference,
) : ViewModel() {

    private val _userData = MutableLiveData<UserData>()
    val userData : LiveData<UserData> get() = _userData

    private val _updateSubscriptionInfo = MutableLiveData<Boolean>()
    val updateSubscriptionInfo : LiveData<Boolean> get() = _updateSubscriptionInfo

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    fun getUserData() {
        viewModelScope.launch {
            repo.getUserData()
                .catch { e:Throwable -> _error.value = e }
                .collect { data ->
                    println("디버깅ㅇㅇㅇㅇㅇㅇㅇㅇㅇ $data")
                    _userData.value = data
                }
        }
    }

    fun updateSubscriptionState(noAds: Boolean) {
        val oldUserData = _userData.value
        val newUserData = _userData.value?.copy(noAds = noAds)
        if(newUserData == null || newUserData == oldUserData) return

        viewModelScope.launch {
            repo.updateUserData(newUserData)
                .catch { e:Throwable -> _error.value = e }
                .collect {
                    preference.isSubscribing = it.noAds
                    _updateSubscriptionInfo.value = it.noAds
                }
        }
    }
}