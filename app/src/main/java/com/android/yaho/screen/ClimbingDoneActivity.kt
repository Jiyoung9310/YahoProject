package com.android.yaho.screen

import android.os.Bundle
import android.widget.Toast
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDoneBinding
import com.android.yaho.viewmodel.ClimbingDoneViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDoneActivity : BindingActivity<ActivityClimbingDoneBinding>(ActivityClimbingDoneBinding::inflate) {
    private val TAG = this::class.java.simpleName

    private val viewModel by viewModel<ClimbingDoneViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initObserve()
    }

    private fun initObserve() {
        viewModel.saveResult.observe(this) {
            Toast.makeText(this, TAG + "save Climbing : $it", Toast.LENGTH_SHORT).show()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "error : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}