package com.android.yaho.repository

import android.util.Log
import com.android.yaho.data.UserClimbingData
import com.android.yaho.local.YahoPreference
import com.android.yaho.local.cache.LiveClimbingCache
import com.android.yaho.local.db.RecordEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

interface ClimbingRepository {
    suspend fun postClimbingData(recordId: String) : Flow<ClimbingResult>
    suspend fun updateVisitMountain() : Flow<ClimbingResult>
    suspend fun getClimbingData(recordId: String) : Flow<RecordEntity?>
    suspend fun getVisitMountain(mountainId: Int): Flow<Int>
    suspend fun getClimbingRecordList() : Flow<List<RecordEntity>>
    suspend fun deleteClimbingData(recordId: String) : Flow<ClimbingResult>
}

@ExperimentalCoroutinesApi
class ClimbingRepositoryImpl(
    private val firestoreDB: FirebaseFirestore,
) : ClimbingRepository, KoinComponent {
    override suspend fun postClimbingData(recordId: String): Flow<ClimbingResult> = callbackFlow {
        val uid = get<YahoPreference>().userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        val recordCache = get<LiveClimbingCache>().getRecord()
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
                            accessUsers.collection("total").document(uid).set(newTotalData)
                                .addOnSuccessListener {
                                    Log.w("ClimbingRepository", "climbing data add : $documentReference")
                                    offer(ClimbingResult.Success)
                                }.addOnFailureListener { e ->
                                    Log.w("ClimbingRepository", "Error adding document", e)
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
                Log.w("ClimbingRepository", "Fail adding document", e)
                offer(ClimbingResult.Fail(e))
            }
        awaitClose { (script as ListenerRegistration).remove() }
    }

    override suspend fun getClimbingData(recordId: String): Flow<RecordEntity?> = callbackFlow {
        val uid = get<YahoPreference>().userId
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
        val uid = get<YahoPreference>().userId
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
        val uid = get<YahoPreference>().userId
        if(uid.isNullOrEmpty()) offer(ClimbingResult.Fail(Throwable("userId 접근 불가")))

        Log.w("ClimbingRepository", "ready to delete climbing data : $recordId")
        val script = firestoreDB.collection("users")
            .document(uid!!)
            .collection("climbingData")
            .document(recordId)
            .delete()
            .addOnSuccessListener { documentReference ->
                Log.w("ClimbingRepository", "climbing data delete : ${documentReference.toString()}")
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