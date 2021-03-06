package com.climbing.yaho.repository

import android.util.Log
import com.climbing.yaho.data.UserClimbingData
import com.climbing.yaho.data.UserData
import com.climbing.yaho.local.YahoPreference
import com.climbing.yaho.local.cache.LiveClimbingCache
import com.climbing.yaho.local.db.RecordEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

interface ClimbingRepository {
    suspend fun postClimbingData(recordId: String) : Flow<ClimbingResult>
    suspend fun updateVisitMountain() : Flow<ClimbingResult>
    suspend fun getClimbingData(recordId: String) : Flow<RecordEntity?>
    suspend fun getVisitMountain(mountainId: Int): Flow<Int>
    suspend fun getClimbingRecordList() : Flow<List<RecordEntity>>
    suspend fun deleteClimbingData(recordId: String) : Flow<ClimbingResult>
}

@ExperimentalCoroutinesApi
class ClimbingRepositoryImpl @Inject constructor(
    private val firestoreDB: FirebaseFirestore,
    private val yahoPreference: YahoPreference,
    private val liveClimbingCache: LiveClimbingCache,
) : ClimbingRepository {
    override suspend fun postClimbingData(recordId: String): Flow<ClimbingResult> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        val recordCache = liveClimbingCache.getRecord()
        recordCache.apply {
            this.recordId = recordId
        }
        Log.w("ClimbingRepository", "ready to climbing data add : $recordCache")
        val accessUsers = firestoreDB.collection("users").document(uid!!)
        val script = accessUsers.collection("climbingData")
            .document(recordId)
            .set(recordCache)
            .addOnSuccessListener { documentReference ->
                try {
                    accessUsers.collection("climbingData")
                        .get()
                        .addOnSuccessListener { result ->
                            val list = result.documents.map {
                                it.toObject(RecordEntity::class.java)
                            }
                            val newTotalData = UserClimbingData()
                            newTotalData.totalCount = list.count()
                            list.forEach {
                                it?.let {
                                    newTotalData.apply {
                                        allHeight += it.maxHeight
                                        allDistance += it.totalDistance
                                        allTime += it.allRunningTime
                                    }
                                }
                            }
                            val newUserData = UserData(
                                allHeight = newTotalData.allHeight,
                                allDistance = newTotalData.allDistance,
                                allTime = newTotalData.allTime,
                                totalCount = newTotalData.totalCount,
                                noAds = yahoPreference.isSubscribing
                            )
                            accessUsers.collection("total").document(uid).set(newUserData)
                                .addOnSuccessListener {
                                    Log.d("ClimbingRepository", "climbing data add : $documentReference")
                                    offer(ClimbingResult.Success)
                                }.addOnFailureListener { e ->
                                    Log.d("ClimbingRepository", "Error adding document", e)
                                    offer(ClimbingResult.Fail(e))
                                }
                        }

                } catch (e: Throwable) {
                    Log.w("ClimbingRepository", "Error adding document", e)
                    offer(ClimbingResult.Fail(e))
                }
            }
            .addOnFailureListener { e ->
                Log.w("ClimbingRepository", "Error adding document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }

    override suspend fun updateVisitMountain(): Flow<ClimbingResult> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        val climbCache = liveClimbingCache.getRecord()
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
                Log.w("ClimbingRepository", "Fail adding document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }

    override suspend fun getClimbingData(recordId: String): Flow<RecordEntity?> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(null)

        val subscription = firestoreDB.collection("users")
            .document(uid!!)
            .collection("climbingData")
            .document(recordId)
            .get()
            .addOnSuccessListener {
                try {
                    val data = it.toObject(RecordEntity::class.java)
                    offer(data)
                    Log.w("ClimbingRepository", "success getClimbingData : $data")
                } catch (e: Throwable) {
                    offer(null)
                    Log.w("ClimbingRepository", "catch getClimbingData error : ${e.message}")
                }
            }
            .addOnFailureListener {
                offer(null)
                Log.w("ClimbingRepository", "fail getClimbingData : ${it.message}")
            }

        awaitClose { (subscription as ListenerRegistration).remove() }
    }

    override suspend fun getVisitMountain(mountainId: Int): Flow<Int> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(0)

        val subscription = firestoreDB.collection("users")
            .document(uid!!)
            .collection("visitList")
            .document(mountainId.toString())
            .get()
            .addOnSuccessListener {
                (it.get("visitCount"))?.let {count ->
                    offer(count.toString().toInt())
                    Log.w("ClimbingRepository", "success get visitCount : $count")
                } ?: run {
                    offer(0)
                    Log.w("ClimbingRepository", "get visitCount just 0")
                }
            }
            .addOnFailureListener {
                offer(0)
                Log.w("ClimbingRepository", "fail get visitCount error : ${it.message}")
            }

        awaitClose { (subscription as ListenerRegistration).remove() }
    }

    override suspend fun getClimbingRecordList(): Flow<List<RecordEntity>> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(emptyList<RecordEntity>())

        var eventCollection: CollectionReference? = null
        try{
            eventCollection = firestoreDB.collection("users")
                .document(uid!!)
                .collection("climbingData")
            Log.w("ClimbingRepository", "ready to getClimbingRecordList")
        } catch (e: Throwable) {
            Log.w("ClimbingRepository", "getClimbingRecordList close")
            close(e)
        }

        val subscription = eventCollection?.addSnapshotListener { snapshot, error ->
            if(snapshot == null) {
                offer(emptyList<RecordEntity>())
                return@addSnapshotListener
            }
            try {
                val data = snapshot.toObjects(RecordEntity::class.java)
                Log.w("ClimbingRepository", "success getClimbingRecordList : $data")
                offer(data)
            } catch (e: Throwable) {
                Log.w("MountainRepository", "getClimbingRecordList error : ${e.message}")
                offer(emptyList<RecordEntity>())
                return@addSnapshotListener
            }
        }
        awaitClose { subscription?.remove() }
    }

    override suspend fun deleteClimbingData(recordId: String): Flow<ClimbingResult> = callbackFlow {
        val uid = yahoPreference.userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        Log.w("ClimbingRepository", "ready to delete climbing data : $recordId")
        val script = firestoreDB.collection("users")
            .document(uid!!)
            .collection("climbingData")
            .document(recordId)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.w("ClimbingRepository", "climbing data delete : $documentReference")
                offer(ClimbingResult.Success)
            }
            .addOnFailureListener { e ->
                Log.w("ClimbingRepository", "Error delete document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }
}

sealed class ClimbingResult {
    object Success : ClimbingResult()
    class Fail(val e: Throwable) : ClimbingResult()
}