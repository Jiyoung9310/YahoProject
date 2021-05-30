package com.climbing.yaho.screen

import android.content.Intent
import android.os.Bundle
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityIntroBinding
import com.climbing.yaho.viewmodel.IntroViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class IntroActivity : BindingActivity<ActivityIntroBinding>(ActivityIntroBinding::inflate) {

    private val viewModel by viewModel<IntroViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObserver()
    }

    private fun initObserver() {
        viewModel.goToHome.observe(this) {
            // go to home screen
            startActivity(Intent(this@IntroActivity, HomeActivity::class.java))
            finish()
        }

        viewModel.goToLogin.observe(this) {
            // go to home screen
            startActivity(Intent(this@IntroActivity, LoginActivity::class.java))
            finish()
        }
    }
}