package com.android.yaho.screen

import android.os.Bundle
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityHomeBinding
import com.android.yaho.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BindingActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private val viewModel by viewModel<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        viewModel.getUserData()
    }

    private fun initView() {

    }

    private fun initObserve() {
        viewModel.userData.observe(this) {
            binding.tvAllHeight.text = getString(R.string.home_height_unit, it.allHeight.toString())
            binding.tvClimbNumber.text = getString(R.string.home_climb_count, it.records.size)
        }
    }
}