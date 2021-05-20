package com.android.yaho.ui

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.base.BindingViewHolder
import com.android.yaho.databinding.ItemClimbingDetailSectionBinding
import com.android.yaho.viewmodel.ClimbingDetailSectionUseCase

class ClimbingDetailSectionAdapter: RecyclerView.Adapter<SectionInfoViewHolder>() {
    private val _sectionList = mutableListOf<ClimbingDetailSectionUseCase>()
    var sectionList : List<ClimbingDetailSectionUseCase>
        get() = _sectionList
        set(value) {
            _sectionList.clear()
            _sectionList.addAll(value)
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SectionInfoViewHolder {
        return SectionInfoViewHolder(parent)
    }

    override fun onBindViewHolder(holder: SectionInfoViewHolder, position: Int) {
        holder.bind(_sectionList[position])
    }

    override fun getItemCount(): Int = _sectionList.count()
}

class SectionInfoViewHolder(parent: ViewGroup) : BindingViewHolder<ItemClimbingDetailSectionBinding>(parent, ItemClimbingDetailSectionBinding::inflate) {
    fun bind(data: ClimbingDetailSectionUseCase) {
        binding.clInfo.isVisible = data.sectionData != null
        binding.tvSectionNumber.text = data.sectionNumber.toString()
        binding.tvSectionTitle.text = data.sectionTitle
        binding.tvSectionData.text = data.sectionPeriod
        binding.tvSectionClimbingTime.text = data.sectionData?.climbingTime
        binding.tvSectionDistance.text = data.sectionData?.distance
    }
}