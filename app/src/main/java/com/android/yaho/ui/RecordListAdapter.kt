package com.android.yaho.ui

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingViewHolder
import com.android.yaho.databinding.ItemRecordHeaderBinding
import com.android.yaho.databinding.ItemRecordListBinding
import com.android.yaho.viewmodel.RecordHeader
import com.android.yaho.viewmodel.RecordUseCase

data class UIItem(
    var id: Any? = null,
    var item: Any = Unit,
    var viewType: Int = 0
) {
    init {
        if (id == null) id = hashCode()
    }
}

class RecordListAdapter : ListAdapter<UIItem, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<UIItem>() {
    override fun areItemsTheSame(oldItem: UIItem, newItem: UIItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: UIItem, newItem: UIItem): Boolean {
        return oldItem == newItem
    }
}){

    companion object {
        const val RECORD_HEADER_VIEW_TYPE = R.layout.item_record_header
        const val RECORD_ITEM_VIEW_TYPE = R.layout.item_record_list
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when(viewType) {
            RECORD_HEADER_VIEW_TYPE -> RecordHeaderViewHolder(parent)
            else -> RecordItemViewHolder(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RecordHeaderViewHolder -> {
                holder.bind(currentList[position] as RecordHeader)
            }
            is RecordItemViewHolder -> {
                holder.bind(currentList[position] as RecordUseCase)
            }
        }
    }

    override fun getItemCount(): Int = currentList.count()
}

class RecordHeaderViewHolder(parent: ViewGroup) : BindingViewHolder<ItemRecordHeaderBinding>(parent, ItemRecordHeaderBinding::inflate) {
    fun bind(data : RecordHeader) {

    }
}

class RecordItemViewHolder(parent: ViewGroup) : BindingViewHolder<ItemRecordListBinding>(parent, ItemRecordListBinding::inflate) {
    fun bind(data : RecordUseCase) {

    }
}