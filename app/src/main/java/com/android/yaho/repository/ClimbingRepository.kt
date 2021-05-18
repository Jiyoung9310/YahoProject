package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.MountainData
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ClimbingRepository {
    suspend fun postClimbingData() : Flow<ClimbingResult>
    suspend fun updateVisitMountain() : Flow<ClimbingResult>
    suspend fun getVisitMountain(mountainId: Int): Flow<Int>
}

@ExperimentalCoroutinesApi
class ClimbingRepositoryImpl(
    private val firestoreDB: FirebaseFirestore,
) : ClimbingRepository, KoinComponent {
    override suspend fun postClimbingData(): Flow<ClimbingResult> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        val climbCache = get<LiveClimbingCache>()
        Log.w("ClimbingRepository", "ready to climbing data add : ${climbCache.getRecord()}")
        val script = firestoreDB.collection("users")
            .document(uid!!)
            .collection("climbingData")
            .document(System.currentTimeMillis().toString())
            .set(climbCache.getRecord())
            .addOnSuccessListener { documentReference ->
                Log.w("ClimbingRepository", "climbing data add : $documentReference")
                offer(ClimbingResult.Success)
            }
            .addOnFailureListener { e ->
                Log.w("ClimbingRepository", "Error adding document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }

    override suspend fun updateVisitMountain(): Flow<ClimbingResult> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        val climbCache = get<LiveClimbingCache>().getRecord()
        val fieldMap = mapOf(
            "mountainId" to climbCache.mountainId,
            "visitCount" to climbCache.mountainVisitCount
        )
        Log.w("ClimbingRepository", "ready to update mountain visit : $fieldMap")

        val script = firestoreDB.collection("users")
            .document(uid!!)
            .collection("visitList")
            .document(climbCache.mountainId.toString())
            .set(fieldMap)
            .addOnSuccessListener { documentReference ->
                Log.w("ClimbingRepository", "update mountain visit : $documentReference")
                offer(ClimbingResult.Success)
            }
            .addOnFailureListener { e ->
                Log.w("ClimbingRepository", "Error adding document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }

    override suspend fun getVisitMountain(mountainId: Int): Flow<Int> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(uid.isNullOrEmpty()) offer(0)

        val subscription = firestoreDB.collection("users")
            .document(uid!!)
            .collection("visitList")
            .document(mountainId.toString())
            .get()
            .addOnSuccessListener {
                (it.get("visitCount"))?.let {count ->
                    offer(count.toString().toInt())
                    Log.w("ClimbingRepository", "get visitCount : $count")
                } ?: run {
                    offer(0)
                    Log.w("ClimbingRepository", "get visitCount just 0")
                }
            }
            .addOnFailureListener {
                offer(0)
                Log.w("ClimbingRepository", "get visitCount error : ${it.message}")
            }

        awaitClose { (subscription as ListenerRegistration).remove() }
    }
}

sealed class ClimbingResult {
    object Success : ClimbingResult()
    class Fail(val e: Throwable) : ClimbingResult()
}