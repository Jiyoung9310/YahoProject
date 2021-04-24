package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface UserDataRepository {
    fun getUserData(): Flow<UserClimbingData>
}

class UserDataRepositoryImpl(private val firestoreDB: FirebaseFirestore) : UserDataRepository, KoinComponent {
    @ExperimentalCoroutinesApi
    override fun getUserData(): Flow<UserClimbingData> = callbackFlow {
        if(get<YahoPreference>().userId == null) offer(UserClimbingData())

        val subscription = firestoreDB.collection("users")
            .document(get<YahoPreference>().userId!!)
            .addSnapshotListener { snapshot, error ->
                try {
                    if(snapshot != null && snapshot.exists()) {
                        val data = snapshot.toObject(UserClimbingData::class.java)
                        data?.let { offer(data) }
                        Log.d("UserDataRepository", "geteUserData : $data")
                    }
                } catch (e: Throwable) {
                    Log.w("UserDataRepository", "firestore error : ${e.message}")
                    return@addSnapshotListener
                }
            }
            awaitClose { subscription.remove() }
    }
}