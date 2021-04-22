package com.android.yaho.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingViewHolder
import com.android.yaho.data.MountainData
import com.android.yaho.databinding.ItemNearMountainBinding

class NearMountainAdapter(private val itemClickAction: () -> Unit) : RecyclerView.Adapter<RecyclerView.ViewHolder>()  {

    private val _mountainList : MutableList<MountainData> = mutableListOf()
    var mountainList : List<MountainData>
        get() = _mountainList
        set(value) {
            _mountainList.clear()
            _mountainList.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return NearMountainViewHolder(parent)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as NearMountainViewHolder).bind(_mountainList[position])
    }

    override fun getItemCount(): Int = _mountainList.size
}

class NearMountainViewHolder(parent: ViewGroup) : BindingViewHolder<ItemNearMountainBinding>(parent, ItemNearMountainBinding::inflate) {

    fun bind(data: MountainData) {
        binding.tvMountainName.text = data.name
        binding.tvHeight.text = binding.root.context.getString(R.string.ready_near_height_unit, data.height)
        binding.tvLevel.text = data.level
    }

}