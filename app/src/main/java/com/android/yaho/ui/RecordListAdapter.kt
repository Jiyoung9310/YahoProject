package com.android.yaho.ui

import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.android.yaho.R
import com.android.yaho.base.BindingViewHolder
import com.android.yaho.databinding.ItemRecordHeaderBinding
import com.android.yaho.databinding.ItemRecordListBinding
import com.android.yaho.databinding.ItemRecordTitleBinding
import com.android.yaho.viewmodel.RecordItem

data class UIItem(
    var id: Any? = null,
    var item: Any = Unit,
    var viewType: Int = 0
) {
    init {
        if (id == null) id = hashCode()
    }
}

class RecordListAdapter(private val clickItem : (String, String) -> Unit,
                        private val selectDate: () -> Unit,
) : ListAdapter<RecordItem, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<RecordItem>() {
    override fun areItemsTheSame(oldItem: RecordItem, newItem: RecordItem): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: RecordItem, newItem: RecordItem): Boolean {
        return oldItem == newItem
    }
}){

    companion object {
        const val RECORD_TITLE_VIEW_TYPE = R.layout.item_record_title
        const val RECORD_HEADER_VIEW_TYPE = R.layout.item_record_header
        const val RECORD_ITEM_VIEW_TYPE = R.layout.item_record_list
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        return when(viewType) {
            RECORD_HEADER_VIEW_TYPE -> RecordHeaderViewHolder(parent)
            RECORD_TITLE_VIEW_TYPE -> RecordTitleViewHolder(parent, selectDate)
            else -> RecordItemViewHolder(parent, clickItem)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when(holder){
            is RecordHeaderViewHolder -> {
                holder.bind(currentList[position] as RecordItem.RecordHeader)
            }
            is RecordItemViewHolder -> {
                holder.bind(currentList[position] as RecordItem.RecordUseCase,
                    !(currentList.lastIndex == position || currentList[position+1] is RecordItem.RecordHeader)
                )
            }
            is RecordTitleViewHolder -> {
                holder.bind(currentList[position] as RecordItem.RecordTitle)
            }
        }
    }

    override fun getItemCount(): Int = currentList.count()

    override fun getItemViewType(position: Int): Int {
        return when(currentList[position]) {
            is RecordItem.RecordTitle -> RECORD_TITLE_VIEW_TYPE
            is RecordItem.RecordHeader -> RECORD_HEADER_VIEW_TYPE
            is RecordItem.RecordUseCase -> RECORD_ITEM_VIEW_TYPE
        }
    }
}

class RecordTitleViewHolder(parent: ViewGroup, private val selectDate: () -> Unit) : BindingViewHolder<ItemRecordTitleBinding>(parent, ItemRecordTitleBinding::inflate) {

    init {
        binding.root.setOnClickListener {
            selectDate.invoke()
        }
    }

    fun bind(data: RecordItem.RecordTitle) {
        binding.tvSelectDate.text = data.selectDate
    }
}

class RecordHeaderViewHolder(parent: ViewGroup) : BindingViewHolder<ItemRecordHeaderBinding>(parent, ItemRecordHeaderBinding::inflate) {
    fun bind(data : RecordItem.RecordHeader) {
        binding.tvHeader.text = data.headerDate
    }
}

class RecordItemViewHolder(parent: ViewGroup, private val clickItem : (String, String) -> Unit) : BindingViewHolder<ItemRecordListBinding>(parent, ItemRecordListBinding::inflate) {
    private var data : RecordItem.RecordUseCase? = null
    init {
        binding.root.setOnClickListener { data?.let { clickItem.invoke(it.recordId, it.mountainName) } }
    }
    fun bind(data : RecordItem.RecordUseCase, showDivider : Boolean) {
        this.data = data
        binding.tvDate.text = data.recordDate
        binding.tvMountainName.text = data.mountainName
        binding.tvRunningTime.text = data.runningTime
        binding.tvDistance.text = data.distance
        binding.divider.isVisible = showDivider
    }
}