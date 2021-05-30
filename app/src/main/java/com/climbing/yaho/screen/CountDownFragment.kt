package com.climbing.yaho.screen

import android.os.Bundle
import android.view.View
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingFragment
import com.climbing.yaho.data.MountainData
import com.climbing.yaho.databinding.FragmentCountdownBinding
import com.climbing.yaho.viewmodel.ReadyViewModel
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
        viewModel.countDown()
    }

    private fun initView() {
        binding.tvMountainName.text = viewModel.selectedMountain?.name
        binding.tvMyChallengeCount.text = getString(R.string.count_unit, viewModel.visitCount)
    }

    private fun initObserve() {
        viewModel.countDownNumber.observe(viewLifecycleOwner) {
            binding.ivNumber.setImageResource(countRes[it-1])
        }
    }
}