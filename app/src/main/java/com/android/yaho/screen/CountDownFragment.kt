package com.android.yaho.screen

import android.os.Bundle
import android.view.View
import com.android.yaho.R
import com.android.yaho.base.BindingFragment
import com.android.yaho.data.MountainData
import com.android.yaho.databinding.FragmentCountdownBinding
import com.android.yaho.screen.ClimbingActivity.Companion.KEY_MOUNTAIN_ID
import com.android.yaho.viewmodel.ReadyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CountDownFragment : BindingFragment<FragmentCountdownBinding>(FragmentCountdownBinding::inflate) {

    private val viewModel: ReadyViewModel by sharedViewModel()
    private lateinit var mountainData : MountainData
    private val countRes = arrayOf(R.drawable.ic_one, R.drawable.ic_two, R.drawable.ic_three)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<MountainData>(ReadyActivity.KEY_SELECT_MOUNTAIN)?.let{
            mountainData = it
        } ?: run {
            viewModel.moveScreen(ReadyActivity.SCREEN_NEAR_MOUNTAIN)
        }

        initView()
        initObserve()
        viewModel.countDown(mountainData.id)
    }

    private fun initView() {
        binding.tvMountainName.text = mountainData.name
    }

    private fun initObserve() {
        viewModel.countDownNumber.observe(viewLifecycleOwner) {
            binding.ivNumber.setImageResource(countRes[it-1])
        }
    }
}