package com.android.yaho.screen

import android.animation.Animator
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityClimbingDoneBinding
import com.android.yaho.ui.ClimbingSaveDialog
import com.android.yaho.viewmodel.ClimbingDoneViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class ClimbingDoneActivity : BindingActivity<ActivityClimbingDoneBinding>(
    ActivityClimbingDoneBinding::inflate
) {
    private val TAG = this::class.java.simpleName

    private val viewModel by viewModel<ClimbingDoneViewModel>()
    private lateinit var connectivityManager : ConnectivityManager
    private var dialog : ClimbingSaveDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectivityManager = getSystemService(ConnectivityManager::class.java)
        dialog = ClimbingSaveDialog(this)
        dialog?.show()

        initView()
        initObserve()
    }
    private fun initView() {

        connectivityManager.registerDefaultNetworkCallback(object :
            ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.e(TAG, "The default network is now: " + network)
                viewModel.saveClimbData()
                dialog?.dismiss()
                binding.lottieDone.playAnimation()
            }
        })

        binding.lottieDone.addAnimatorListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(p0: Animator?) {

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
            Toast.makeText(this, getString(R.string.climbing_save_done), Toast.LENGTH_LONG).show()
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