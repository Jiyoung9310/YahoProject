package com.climbing.yaho.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.climbing.yaho.R
import com.climbing.yaho.databinding.ItemNearMountainBinding
import com.climbing.yaho.base.BindingViewHolder
import com.climbing.yaho.data.MountainData

class NearMountainAdapter(private val itemClickAction: (MountainData) -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    private val _mountainList : MutableList<MountainData> = mutableListOf()
    var mountainList : List<MountainData>
        get() = _mountainList
        set(value) {
            _mountainList.clear()
            _mountainList.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NearMountainViewHolder(parent, itemClickAction)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NearMountainViewHolder).bind(_mountainList[position])
    }

    override fun getItemCount(): Int = _mountainList.size
}

class NearMountainViewHolder(parent: ViewGroup, private val itemClickAction: (MountainData) -> Unit) : BindingViewHolder<ItemNearMountainBinding>(parent, ItemNearMountainBinding::inflate) {

    private var mountainData : MountainData? = null

    init{
        binding.root.setOnClickListener {
            mountainData?.let { itemClickAction.invoke(it) }
        }
    }

    fun bind(data: MountainData) {
        mountainData = data
        binding.tvMountainName.text = data.name
        binding.tvHeight.text = binding.root.context.getString(R.string.meter_unit, data.height)

        val levelInt = when(data.level) {
            "최상" -> 4
            "상" -> 3
            "중" -> 2
            else -> 1
        }

        val filledRes = R.drawable.bg_near_mountain_level_filled
        val unfilledRes = R.drawable.bg_near_mountain_level_unfilled

        binding.vLevel4.setBackgroundResource(if(levelInt >= 4) filledRes else unfilledRes)
        binding.vLevel3.setBackgroundResource(if(levelInt >= 3) filledRes else unfilledRes)
        binding.vLevel2.setBackgroundResource(if(levelInt >= 2) filledRes else unfilledRes)
        binding.vLevel1.setBackgroundResource(if(levelInt >= 1) filledRes else unfilledRes)
    }

}