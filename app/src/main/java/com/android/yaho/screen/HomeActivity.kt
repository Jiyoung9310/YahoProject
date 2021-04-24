package com.android.yaho.screen

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingActivity
import com.android.yaho.databinding.ActivityHomeBinding
import com.android.yaho.dp
import com.android.yaho.ui.HomeMenuAdapter
import com.android.yaho.viewmodel.HomeViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class HomeActivity : BindingActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private val viewModel by viewModel<HomeViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initView()
        initObserve()
        viewModel.getUserData()
    }

    private fun initView() {
        binding.rvMenu.apply {
            layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
            adapter = HomeMenuAdapter(
                startClimbingClickAction = {
                    // 등산 기록하기 화면으로 이동
                    startActivity(Intent(this@HomeActivity, ReadyActivity::class.java))
                },
                myClimbsClickAction = {
                    // 등산 기록 확인하기 화면으로 이동
                },
                removeAdsClickAction = {
                    // 광고 제거 결제 화면으로 이동 
                }
            )
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)
                    outRect.left = 12.dp
                    outRect.right = 12.dp

                    view.layoutParams.width = (parent.width * 0.8).toInt()
                }
            })
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)

                    val offset = 20.dp
                    val itemCount = state.itemCount
                    val childPosition = parent.getChildAdapterPosition(view)
                    if(childPosition == 0) {
                        outRect.left = offset
                    } else if (childPosition == itemCount - 1) {
                        outRect.right = offset
                    }
                }
            })

            if (onFlingListener == null) PagerSnapHelper().attachToRecyclerView(this)
        }


    }

    private fun initObserve() {
        viewModel.userData.observe(this) {
            binding.tvAllHeight.text = getString(R.string.home_height_unit, it.allHeight.toString())
            binding.tvClimbNumber.text = getString(R.string.home_climb_count, it.records.size)
        }
    }
}