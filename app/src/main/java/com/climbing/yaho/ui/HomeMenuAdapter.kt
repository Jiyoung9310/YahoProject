package com.climbing.yaho.ui

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.climbing.yaho.R
import com.climbing.yaho.base.BindingViewHolder
import com.climbing.yaho.databinding.ItemHomeMenuBinding
import com.climbing.yaho.dp
import com.climbing.yaho.ui.HomeMenuAdapter.Companion.VIEW_TYPE_MY_CLIMBS
import com.climbing.yaho.ui.HomeMenuAdapter.Companion.VIEW_TYPE_REMOVE_ADS
import com.climbing.yaho.ui.HomeMenuAdapter.Companion.VIEW_TYPE_REMOVE_ADS_DONE
import com.climbing.yaho.ui.HomeMenuAdapter.Companion.VIEW_TYPE_START_CLIMBING

class HomeMenuAdapter(
    private val startClimbingClickAction: () -> Unit,
    private val myClimbsClickAction: () -> Unit,
    private val removeAdsClickAction: () -> Unit,
) : RecyclerView.Adapter<HomeMenuViewHolder>() {

    companion object {
        const val VIEW_TYPE_START_CLIMBING = 0
        const val VIEW_TYPE_MY_CLIMBS = 1
        const val VIEW_TYPE_REMOVE_ADS = 2
        const val VIEW_TYPE_REMOVE_ADS_DONE = 3
    }

    private val _menuList : MutableList<Int> = mutableListOf(
        VIEW_TYPE_START_CLIMBING,
        VIEW_TYPE_MY_CLIMBS,
        VIEW_TYPE_REMOVE_ADS,
        VIEW_TYPE_REMOVE_ADS_DONE
    )
    var menuList : List<Int>
    get() = _menuList
    set(value) {
        _menuList.clear()
        _menuList.addAll(value)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HomeMenuViewHolder {
        return HomeMenuViewHolder(parent).apply {
            binding.root.setOnClickListener {
                when(viewType) {
                    VIEW_TYPE_START_CLIMBING -> startClimbingClickAction.invoke()
                    VIEW_TYPE_MY_CLIMBS -> myClimbsClickAction.invoke()
                    VIEW_TYPE_REMOVE_ADS -> removeAdsClickAction.invoke()
                }
            }
        }
    }

    override fun onBindViewHolder(holder: HomeMenuViewHolder, position: Int) {
        holder.bind(menuList[position])
    }

    override fun getItemCount(): Int = menuList.size

    override fun getItemViewType(position: Int): Int = menuList[position]
}



class HomeMenuViewHolder(parent: ViewGroup) : BindingViewHolder<ItemHomeMenuBinding>(parent, ItemHomeMenuBinding::inflate) {

    fun bind(type: Int) {
        when(type) {
            VIEW_TYPE_START_CLIMBING -> {
                binding.container.setBackgroundResource(R.drawable.bg_home_menu_green)
                binding.tvMenuTitle.setText(R.string.home_menu_start_climbing)
                binding.btnMenu.isVisible = false
                binding.btnImage.isVisible = true
            }
            VIEW_TYPE_MY_CLIMBS -> {
                binding.container.setBackgroundResource(R.drawable.bg_home_menu_olive)
                binding.tvMenuTitle.setText(R.string.home_menu_my_climbs)
                binding.btnMenu.setText(R.string.home_menu_btn_go_my_climbs)
                binding.btnMenu.isVisible = true
                binding.btnImage.isVisible = false
            }
            VIEW_TYPE_REMOVE_ADS -> {
                binding.container.setBackgroundResource(R.drawable.bg_home_menu_violet)
                binding.tvMenuTitle.setText(R.string.home_menu_remove_ads)
                binding.btnMenu.setText(R.string.home_menu_btn_remove_ads)
                binding.btnMenu.isVisible = true
                binding.btnImage.isVisible = false
            }
            VIEW_TYPE_REMOVE_ADS_DONE -> {
                binding.container.setBackgroundResource(R.drawable.bg_home_menu_violet)
                binding.tvMenuTitle.setText(R.string.home_menu_remove_ads)
                binding.btnMenu.setText(R.string.home_menu_btn_remove_ads_done)
                binding.btnMenu.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_check, 0)
                binding.btnMenu.isVisible = true
                binding.btnImage.isVisible = false
            }
        }
    }
}
