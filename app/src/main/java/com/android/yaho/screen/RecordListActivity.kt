package com.android.yaho.screen

import android.content.DialogInterface
import android.graphics.Canvas
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AlertDialogLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityRecordListBinding
import com.android.yaho.dp
import com.android.yaho.screen.ClimbingDetailActivity.Companion.startClimbingDetailActivity
import com.android.yaho.ui.RecordListAdapter
import com.android.yaho.ui.SimpleDividerItemDecoration
import com.android.yaho.viewmodel.RecordListViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class RecordListActivity : BindingActivity<ActivityRecordListBinding>(ActivityRecordListBinding::inflate) {

    private val viewModel by viewModel<RecordListViewModel>()
    private lateinit var recordListAdapter : RecordListAdapter
    private var recordHeaderList : Array<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
    }

    private fun initView() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        recordListAdapter = RecordListAdapter(
            clickItem = {
                startClimbingDetailActivity(
                    activity = this,
                    climbingId = it
                )
            },
            selectDate = {
                if(!recordHeaderList.isNullOrEmpty()) {
                    AlertDialog.Builder(this@RecordListActivity)
                        .setSingleChoiceItems(recordHeaderList, 0) { dialog, which ->
                            viewModel.onSelectDate(which)
                            dialog.dismiss()
                        }.create().show()
                }
            })

        binding.rvList.apply {
            layoutManager = LinearLayoutManager(this@RecordListActivity, RecyclerView.VERTICAL, false)
            adapter = recordListAdapter
            addItemDecoration(
                SimpleDividerItemDecoration(
                    context,
                    orientation = RecyclerView.VERTICAL,
                    startIndex = 1,
                    space = 20.dp
                ).apply {
                    ContextCompat.getDrawable(context, R.drawable.item_decoration_divider)?.let {
                        setDrawable(it)
                    }
                }
            )
        }
    }

    private fun initObserve() {
        viewModel.recordList.observe(this) {
            recordListAdapter.submitList(it)
        }
        viewModel.recordHeaderDateList.observe(this) { list ->
            recordHeaderList = list
        }
    }
}