package com.climbing.yaho.screen

import android.os.Bundle
import com.climbing.yaho.base.BindingActivity
import com.climbing.yaho.databinding.ActivityPrivatePolicyBinding

class PrivatePolicyActivity : BindingActivity<ActivityPrivatePolicyBinding>(ActivityPrivatePolicyBinding::inflate) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.viewPolicy.loadUrl("file:///android_asset/file/private_policy_yaho.html")
    }
}