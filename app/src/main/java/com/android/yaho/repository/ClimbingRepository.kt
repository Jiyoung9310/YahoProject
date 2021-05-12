package com.android.yaho.repository

import android.util.Log
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ClimbingRepository {
    fun postClimbingData() : Flow<ClimbingResult>
}

class ClimbingRepositoryImpl(
    private val firestoreDB: FirebaseFirestore,
) : ClimbingRepository, KoinComponent {
    @ExperimentalCoroutinesApi
    override fun postClimbingData(): Flow<ClimbingResult> = callbackFlow {
        val uid = get<YahoPreference>().userId
        val climbCache = get<LiveClimbingCache>()
        Log.w("ClimbingRepository", "ready to climbing data add : ${climbCache.getRecord()}")
        if(!uid.isNullOrEmpty()) {
            val script = firestoreDB.collection("users")
                .document(uid)
                .collection("climbingData")
                .add(climbCache.getRecord())
                .addOnSuccessListener { documentReference ->
                    Log.w("ClimbingRepository", "climbing data add : $documentReference")
                    if (documentReference != null) {
                        offer(ClimbingResult.Success)
                    } else {
                        offer(ClimbingResult.Fail(Throwable("Climbing Document 생성 실패")))
                    }
                }
                .addOnFailureListener { e ->
                    Log.w("ClimbingRepository", "Error adding document", e)
                    offer(ClimbingResult.Fail(e))
                }
            awaitClose { (script as ListenerRegistration).remove() }
        }
    }
}

sealed class ClimbingResult {
    object Success : ClimbingResult()
    class Fail(val e: Throwable) : ClimbingResult()
}