package com.climbing.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.climbing.yaho.repository.LoginRepository
import com.climbing.yaho.repository.LoginResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel(private val repo : LoginRepository) : ViewModel() {

    private val _goToHome = MutableLiveData<Unit>()
    val goToHome: LiveData<Unit> get() = _goToHome

    private val _enablePhoneNumber = MutableLiveData<Boolean>()
    val enablePhoneNumber: LiveData<Boolean> get() = _enablePhoneNumber

    private val _enableVerifyEdit = MutableLiveData<Boolean>()
    val enableVerifyEdit: LiveData<Boolean> get() = _enableVerifyEdit

    private lateinit var verificationId : String

    private val _checkVerifyCode = MutableLiveData<Pair<String, String>>()
    val checkVerifyCode: LiveData<Pair<String, String>> get() = _checkVerifyCode

    private val _error = MutableLiveData<Throwable>()
    val error: LiveData<Throwable> get() = _error

    init {
        if(repo.getUserID() != null) _goToHome.value = Unit
    }

    fun getPhoneNumber(phoneNum : String) {
        _enablePhoneNumber.value = phoneNum.count() > 3
    }

    fun getCodeNumber(codeNum : String) {
        _enableVerifyEdit.value = codeNum.isNotEmpty()
    }

    fun getVerifyNumber(verificationId: String) {
        this.verificationId = verificationId
    }

    fun onClickCheckCode(code: String) {
        _checkVerifyCode.value = verificationId to code
    }

    fun saveUserId(uid: String?) {
        uid?.let {
            viewModelScope.launch {
                repo.saveUserID(uid)
                    .catch { e : Throwable ->
                        _error.value = e
                    }
                    .onEach {
                        getNetworkResult(it)
                    }
                    .collect()
            }

        }

    }

    /*fun signUp() {
        viewModelScope.launch {
            repo.updateNewUser()
                .catch { e : Throwable ->
                    _error.value = e
                }
                .onEach {
                    getNetworkResult(it)
                }
                .collect()


        }
    }*/

    private fun getNetworkResult(result: LoginResult) {
        when(result) {
            LoginResult.LoginSuccess, LoginResult.NewUserSignUp -> _goToHome.value = Unit
            LoginResult.NoLoginData -> {}
            is LoginResult.SignUpFail -> _error.value = result.e
        }
    }
}