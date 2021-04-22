package com.android.yaho.base

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding

abstract class BindingViewHolder<T: ViewBinding>(
    parent: ViewGroup,
    inflate: (LayoutInflater, ViewGroup?, Boolean) -> T,
    val binding: T = inflate.invoke(LayoutInflater.from(parent.context), parent, false)
) : RecyclerView.ViewHolder(binding.root)