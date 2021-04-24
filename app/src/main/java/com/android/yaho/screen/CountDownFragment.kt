package com.android.yaho.screen

import android.os.Bundle
import android.view.View
import com.android.yaho.base.BindingFragment
import com.android.yaho.data.MountainData
import com.android.yaho.databinding.FragmentCountdownBinding
import com.android.yaho.viewmodel.ReadyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class CountDownFragment : BindingFragment<FragmentCountdownBinding>(FragmentCountdownBinding::inflate) {

    private val viewModel: ReadyViewModel by sharedViewModel()
    private lateinit var mountainData : MountainData

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<MountainData>(ReadyActivity.KEY_SELECT_MOUNTAIN)?.let{
            mountainData = it
        } ?: run {
            viewModel.moveScreen(ReadyActivity.SCREEN_NEAR_MOUNTAIN)
        }

        initView()
    }

    private fun initView() {
        binding.tvMountainName.text = mountainData.name
    }
}