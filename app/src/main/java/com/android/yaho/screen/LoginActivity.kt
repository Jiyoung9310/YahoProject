package com.android.yaho.screen

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityLoginBinding
import com.android.yaho.viewmodel.LoginViewModel
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
        binding.etPhoneNumber.setText("+1 650-555-3535")
        binding.btnGetCode.setOnClickListener {
            startVerifyCode(binding.etPhoneNumber.text.toString())
        }
        binding.btnCheckCode.setOnClickListener {
            viewModel.onClickCheckCode(binding.etCode.text.toString())
        }
    }

    private fun initObserve() {
        viewModel.goToHome.observe(this) {
            // go to home screen
            startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
            finish()
        }

        viewModel.enableVerifyEdit.observe(this) {
            binding.etCode.isEnabled = it
            binding.btnCheckCode.isEnabled = it
            binding.etCode.setText("654321")
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
                    viewModel.getVerifyNumber(verificationId)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    Toast.makeText(this@LoginActivity, "Success : ${task.result?.user?.uid}", Toast.LENGTH_SHORT).show()
                    viewModel.saveUserId(task.result?.user?.uid)
                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                        Toast.makeText(this@LoginActivity, "verification code invalid : ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                    // Update UI
                    Toast.makeText(this@LoginActivity, "Error", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
