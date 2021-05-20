package com.android.yaho.screen

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityRecordListBinding
import com.android.yaho.ui.RecordListAdapter
import com.android.yaho.viewmodel.RecordListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    private val viewModel by viewModel<RecordListViewModel>()
    private lateinit var recordListAdapter : RecordListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@RecordListActivity, RecyclerView.VERTICAL, false)
            adapter
        }
    }

    private fun initObserve() {
        viewModel.recordList.observe(this) {
            recordListAdapter.submitList(it)
        }
    }
}