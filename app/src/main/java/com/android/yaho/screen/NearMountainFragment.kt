package com.android.yaho.screen

import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.base.BindingFragment
import com.android.yaho.databinding.FragmentNearMountainBinding
import com.android.yaho.dp
import com.android.yaho.ui.NearMountainAdapter
import com.android.yaho.viewmodel.ReadyViewModel
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class NearMountainFragment: BindingFragment<FragmentNearMountainBinding>(FragmentNearMountainBinding::inflate) {

    private val viewModel : ReadyViewModel by sharedViewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel
        initView()
        initObserve()
    }

    private fun initView() {
        binding.rvNearMountain.apply {
            layoutManager = GridLayoutManager(context, 2)
            adapter = NearMountainAdapter {

            }
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
                    if(layoutManager.spanSizeLookup.getSpanSize(parent.getChildAdapterPosition(view)) != 1) return
                    val lp = parent.getChildViewHolder(view).itemView.layoutParams as GridLayoutManager.LayoutParams
                    when(lp.spanIndex) {
                        0 -> outRect.set(0, 0, spacing * 2, spacing * 2)
                        else -> outRect.set(0, 0, 0, spacing * 2)
                    }
                }
            })
        }
    }

    private fun initObserve() {

    }

}