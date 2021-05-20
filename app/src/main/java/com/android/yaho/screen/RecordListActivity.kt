package com.android.yaho.screen

import android.os.Bundle
import android.widget.Toast
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityRecordListBinding
import com.android.yaho.viewmodel.RecordListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    private val viewModel by viewModel<RecordListViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {

    }

    private fun initObserve() {
        viewModel.recordList.observe(this) {
            Toast.makeText(this, "${it.map { it.recordId }.toString()}", Toast.LENGTH_SHORT).show()
        }
    }
}