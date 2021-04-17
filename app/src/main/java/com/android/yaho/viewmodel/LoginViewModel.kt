package com.android.yaho.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.yaho.repository.LoginRepository
import com.android.yaho.repository.LoginResult
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class LoginViewModel(private val repo : LoginRepository) : ViewModel() {

    private val _goToHome = MutableLiveData<Unit>()
    val goToHome: LiveData<Unit> get() = _goToHome

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

    fun getVerifyNumber(verificationId: String) {
        this.verificationId = verificationId
        _enableVerifyEdit.value = true
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

    fun signUp() {
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
    }

    private fun getNetworkResult(result: LoginResult) {
        when(result) {
            LoginResult.LoginSuccess -> _goToHome.value = Unit
            LoginResult.NoLoginData -> signUp()
            LoginResult.NewUserSignUp -> _goToHome.value = Unit
            is LoginResult.SignUpFail -> _error.value = result.e
        }
    }
}