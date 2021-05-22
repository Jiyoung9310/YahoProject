package com.android.yaho.screen

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDoneBinding
import com.android.yaho.repository.ClimbingResult
import com.android.yaho.viewmodel.ClimbingDoneViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDoneActivity : BindingActivity<ActivityClimbingDoneBinding>(ActivityClimbingDoneBinding::inflate) {
    private val TAG = this::class.java.simpleName

    private val viewModel by viewModel<ClimbingDoneViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }
    private fun initView() {
        binding.lottieDone.addAnimatorListener(object: Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {
                viewModel.saveClimbData()
            }

            override fun onAnimationEnd(p0: Animator?) {
                viewModel.animationEnd()
            }

            override fun onAnimationCancel(p0: Animator?) {

            }

            override fun onAnimationRepeat(p0: Animator?) {

            }
        })
    }

    private fun initObserve() {
        viewModel.saveResult.observe(this) {
            Toast.makeText(this, "등산 데이터 저장 완료!", Toast.LENGTH_SHORT).show()
        }

        viewModel.goToDetail.observe(this) {
            ClimbingDetailActivity.startClimbingDetailActivity(this, it)
            finish()
        }

        viewModel.error.observe(this) {
            Toast.makeText(this, "error : ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }
}