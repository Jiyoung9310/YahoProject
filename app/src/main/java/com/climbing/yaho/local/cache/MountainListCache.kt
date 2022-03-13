package com.climbing.yaho.local.cache

import android.util.Log
import com.climbing.yaho.data.MountainData
import com.climbing.yaho.repository.MountainRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject


class MountainListCache @Inject constructor(
    private val mountainRepository: MountainRepository,
) {

    private val _data = mutableMapOf<Int, MountainData>()
    val data: Map<Int, MountainData>
        get() = _data

    @InternalCoroutinesApi
    fun initialize() = GlobalScope.launch {
        mountainRepository.getMountainList()
            .catch { e : Throwable ->
                Log.d("MountainListCache", "caching error : ${e.message}")
            }.collect {
                _data.putAll(it.map { data ->
                    data.id to data
                })
            }
    }

    fun get(id: Int) : MountainData? = _data[id]

    fun getAddress(id: Int) : String = _data[id]?.address ?: ""
}