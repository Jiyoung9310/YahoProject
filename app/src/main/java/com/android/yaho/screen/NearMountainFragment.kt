package com.android.yaho.screen

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.base.BindingFragment
import com.android.yaho.databinding.FragmentNearMountainBinding
import com.android.yaho.dp
import com.android.yaho.screen.ReadyActivity.Companion.KEY_SELECT_MOUNTAIN
import com.android.yaho.screen.ReadyActivity.Companion.SCREEN_SELECT_MOUNTAIN
import com.android.yaho.ui.NearMountainAdapter
import com.android.yaho.viewmodel.ReadyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NearMountainFragment: BindingFragment<FragmentNearMountainBinding>(FragmentNearMountainBinding::inflate) {

    private val viewModel : ReadyViewModel by sharedViewModel()
    private lateinit var nearMountainAdapter: NearMountainAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initView()
        initObserve()
        viewModel.onClickMyCurrentLocation()
    }

    private fun initView() {
        binding.btnNearMountainLocation.setOnClickListener {
            viewModel.onClickMyCurrentLocation()
        }

        nearMountainAdapter = NearMountainAdapter {
            viewModel.onClickMountain(it)
        }

        binding.rvNearMountain.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = nearMountainAdapter
            addItemDecoration(object : RecyclerView.ItemDecoration() {
                override fun getItemOffsets(
                    outRect: Rect,
                    view: View,
                    parent: RecyclerView,
                    state: RecyclerView.State
                ) {
                    super.getItemOffsets(outRect, view, parent, state)

                    val spacing = 6.dp
                    val layoutManager = parent.layoutManager as GridLayoutManager
                    if (layoutManager.spanSizeLookup.getSpanSize(parent.getChildAdapterPosition(view)) != 1) return
                    val lp =
                        parent.getChildViewHolder(view).itemView.layoutParams as GridLayoutManager.LayoutParams
                    when (lp.spanIndex) {
                        0 -> outRect.set(0, 0, spacing * 2, spacing * 2)
                        else -> outRect.set(0, 0, 0, spacing * 2)
                    }
                }
            })
        }
    }

    private fun initObserve() {
        viewModel.showLoading.observe(viewLifecycleOwner) {
            binding.progressCircular.isVisible = it
        }

        viewModel.nearByList.observe(viewLifecycleOwner) {
            nearMountainAdapter.mountainList = it
        }

        viewModel.clickMountain.observe(viewLifecycleOwner) {
            viewModel.moveScreen(
                SCREEN_SELECT_MOUNTAIN,
                Bundle().apply {
                    putParcelable(KEY_SELECT_MOUNTAIN, it)
                }
            )
        }

        viewModel.myLocation.observe(viewLifecycleOwner) { location ->
            viewModel.getNearMountain(location)
        }
    }

}