package com.android.yaho.screen

import android.os.Bundle
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityReadyBinding
import com.android.yaho.viewmodel.ReadyViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ReadyActivity: BindingActivity<ActivityReadyBinding>(ActivityReadyBinding::inflate)  {

    companion object {
        const val SCREEN_NEAR_MOUNTAIN = "SCREEN_NEAR_MOUNTAIN"
        const val SCREEN_SELECT_MOUNTAIN = "SCREEN_SELECT_MOUNTAIN"
        const val SCREEN_COUNT_DOWN = "SCREEN_COUNT_DOWN"
    }

    private val viewModel by viewModel<ReadyViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        initView()
        initObserve()
        viewModel.moveScreen(SCREEN_NEAR_MOUNTAIN)
    }

    private fun initView() {

    }

    private fun initObserve() {
        viewModel.moveScreen.observe(this) {
            when(it) {
                SCREEN_NEAR_MOUNTAIN -> {

                }
                SCREEN_SELECT_MOUNTAIN -> {

                }
                SCREEN_COUNT_DOWN -> {

                }
                else -> {

                }
            }
        }
    }
}