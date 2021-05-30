package com.android.yaho.di

import android.content.Context
import androidx.annotation.StringRes

interface ContextDelegate {
    fun getContext(): Context
    fun getString(resId: Int): String
    fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String
    fun getColor(resId: Int): Int
}

class ContextDelegateImpl(private val context: Context) : ContextDelegate {
    override fun getContext(): Context = context

    override fun getString(resId: Int): String = context.getString(resId)

    override fun getColor(resId: Int): Int = context.getColor(resId)

    override fun getString(@StringRes resId: Int, vararg formatArgs: Any?): String {
        return context.resources.getString(resId, *formatArgs)
    }
}