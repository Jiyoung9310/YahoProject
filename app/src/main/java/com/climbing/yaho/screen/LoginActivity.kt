package com.climbing.yaho.screen

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityLoginBinding
import com.climbing.yaho.viewmodel.LoginViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class LoginActivity : BindingActivity<ActivityLoginBinding>(ActivityLoginBinding::inflate){
    private val TAG = this::class.simpleName


    private val viewModel by viewModel<LoginViewModel>()
    private val firebaseAuth by inject<FirebaseAuth>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        binding.etPhoneNumber.requestFocus()
        binding.etPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.getPhoneNumber(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {

            }
        })

        binding.etCode.addTextChangedListener {
            viewModel.getCodeNumber(it.toString())
        }

        binding.etPhoneNumber.setText("+1 650-555-3535")
        binding.btnGetCode.setOnClickListener {
            startVerifyCode(binding.etPhoneNumber.text.toString())
        }
        binding.btnCheckCode.setOnClickListener {
            viewModel.onClickCheckCode(binding.etCode.text.toString())
        }

        //키보드 보이게 하는 부분
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
    }

    private fun initObserve() {
        viewModel.goToHome.observe(this) {
            // go to home screen
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }

        viewModel.enablePhoneNumber.observe(this) {
            binding.btnGetCode.isEnabled = it
        }

        viewModel.enableVerifyEdit.observe(this) {
            binding.btnCheckCode.isEnabled = it
//            binding.etCode.setText("654321")
        }

        viewModel.checkVerifyCode.observe(this) { (verificationId, code) ->
            val credential = PhoneAuthProvider.getCredential(verificationId, code)
            signInWithPhoneAuthCredential(credential)
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "Oops!! Error : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun startVerifyCode(phoneNumber: String) {
        Log.d(TAG, "try verifyPhoneNumber:$phoneNumber")
        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this)                 // Activity (for callback binding)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    // 사용자의 전화번호가 정상적으로 인증된 것이므로
                    // 콜백에 전달된 PhoneAuthCredential 객체를 사용하여 사용자를 로그인 처리할 수 있습니다.
                    Log.d(TAG, "onVerificationCompleted:$credential")
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    // 요청에 잘못된 전화번호 또는 인증 코드가 지정된 경우와 같이
                    // 잘못된 인증 요청에 대한 응답으로 호출됩니다.
                    Log.w(TAG, "onVerificationFailed", e)

                    if (e is FirebaseAuthInvalidCredentialsException) {
                        // Invalid request
                        Toast.makeText(this@LoginActivity, "Invalid request", Toast.LENGTH_SHORT).show()
                    } else if (e is FirebaseTooManyRequestsException) {
                        // The SMS quota for the project has been exceeded
                        Toast.makeText(this@LoginActivity, "SMS 할당량이 초과되었습니다.", Toast.LENGTH_SHORT).show()
                    }

                    // Show a message and update the UI
                    Toast.makeText(this@LoginActivity, "onVerificationFailed", Toast.LENGTH_SHORT).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    Log.d(TAG, "onCodeSent:$verificationId")
                    binding.etCode.apply {
                        isEnabled = true
                        requestFocus()
                    }
                    viewModel.getVerifyNumber(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                binding.tvCodeCheck.isVisible = !task.isSuccessful
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    viewModel.saveUserId(task.result?.user?.uid)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                }
            }
    }
}
