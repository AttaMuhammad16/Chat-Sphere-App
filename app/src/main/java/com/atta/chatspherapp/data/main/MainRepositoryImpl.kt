package com.atta.chatspherapp.data.main


import android.util.Log
import com.atta.chatspherapp.models.ContactModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.Constants.NUMBEROFMESSAGES
import com.atta.chatspherapp.utils.Constants.PHONE
import com.atta.chatspherapp.utils.MyExtensions.logT
import com.atta.chatspherapp.utils.MyExtensions.shrink
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.random.Random
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.isAccessible


class MainRepositoryImpl @Inject constructor(private val databaseReference: DatabaseReference) : MainRepository {

    override fun <T> collectAnyModel(path: String, clazz: Class<T>): Flow<List<T>> = callbackFlow {
        path.logT("collectAnyModel->path","path")
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.logT("collectAnyModel->dataSnapshot:","firebase")
                val messagesList = mutableListOf<T>()
                for (childSnapshot in dataSnapshot.children) {
                    val message = childSnapshot.getValue(clazz)
                    message?.let {
                        messagesList.add(it)
                    }
                }
                trySend(messagesList as List<T>).isSuccess
            }
            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }
        databaseReference.child(path).addValueEventListener(valueEventListener)
        awaitClose {
            databaseReference.child(path).removeEventListener(valueEventListener)
        }
    }

    override fun <T> collectAnyModel(
        path: String,
        clazz: Class<T>,
        numberOfItems: Int ,
    ): Flow<List<T>> = callbackFlow {

        path.logT("collectAnyModel->path ", "path")
        numberOfItems.logT("numberOfItems", "path")
        clazz.simpleName.logT("clazz.simpleName")

        val query:Query =  if (numberOfItems == 0){
            databaseReference.child(path)
        }else{
            databaseReference.child(path).limitToLast(numberOfItems)
        }
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.logT("collectAnyModel->dataSnapshot:", "firebase")
                val messagesList = mutableListOf<T>()
                dataSnapshot.children.forEach {
                    val message = it.getValue(clazz)
                    message?.let { m ->
                        messagesList.add(m)
                    }
                }
                trySend(messagesList).isSuccess
            }

            override fun onCancelled(databaseError: DatabaseError) {
                close(databaseError.toException())
            }
        }
        query.addValueEventListener(valueEventListener)
        awaitClose {
            query.removeEventListener(valueEventListener)
        }
    }


    override suspend fun <T : Any> uploadAnyModel(path: String, model: T): MyResult<String> {
        path.logT("uploadAnyModel->path","path")
        return try {
            val keyProperty = model::class.declaredMemberProperties.find { it.name == "key" }
            if (keyProperty != null) {
                keyProperty.isAccessible = true
                val key = keyProperty.call(model)?.toString() ?: ""
                val updatedKey = key.ifEmpty {
                    databaseReference.push().key.toString().also { newKey ->
                        if (keyProperty is KMutableProperty<*>) {
                            (keyProperty as KMutableProperty<*>).setter.call(model, newKey)
                        } else {
                            throw IllegalStateException("The 'key' property is not mutable")
                        }
                    }
                }
                databaseReference.child(path).child(updatedKey).setValue(model.shrink())
                MyResult.Success(if (key.isEmpty()) updatedKey else "Updated")
            } else {
                databaseReference.child(path).setValue(model.shrink())
                MyResult.Success("Success")
            }


        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun deleteAnyModel(path: String): MyResult<String> {
        path.logT("deleteAnyModel->path","path")
        return try {
            databaseReference.child(path).removeValue().await()
            MyResult.Success("deleted Successfully")
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun <T> getAnyData(path: String, clazz: Class<T>): T? {
        path.logT("getAnyData->path","path")
        return try {
            val snapshot = databaseReference.child(path).get().await()
            snapshot.logT("getAnyData->snapshot","firebase")
            snapshot.getValue(clazz)
        } catch (e: Exception) {
            Log.e("TAG", "Failed to retrieve data: ${e.message}")
            null
        }
    }


    override suspend fun checkPhoneNumberExists(path: String, phoneNumber: String): MyResult<Boolean> = withContext(Dispatchers.IO) {
        val dataSnapshot = databaseReference.child(path).get().await()
        for (contactSnapshot in dataSnapshot.children) {
            val contact = contactSnapshot.getValue(ContactModel::class.java)
            if (contact?.phone == phoneNumber) {
                return@withContext MyResult.Success(true)
            }
        }
        return@withContext MyResult.Error("Does not exist.")
    }

    override suspend fun updateNumberOfMessages(path: String): MyResult<String> {
        val map=HashMap<String,Any>()
        map[NUMBEROFMESSAGES] = 0
        return try{
            databaseReference.child(path).updateChildren(map).await()
            MyResult.Success("Updated")
        }catch (e:Exception){
            MyResult.Error(e.message.toString())
        }
    }


    override suspend fun uploadMap(path: String, dataMap: HashMap<String,Any>): MyResult<Boolean> {
        return try {
            databaseReference.child(path).updateChildren(dataMap).await()
            MyResult.Success(true)
        } catch (e: Exception) {
            MyResult.Error(e.message.toString())
        }
    }

    override suspend fun getAnyModelFlow(path: String,userModel: UserModel): Flow<UserModel>  = callbackFlow{
        val childRef = databaseReference.child(path)
        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val model = snapshot.getValue(userModel::class.java)
                if (model!=null){
                    trySend(model).isSuccess
                }
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        childRef.addValueEventListener(valueEventListener)
        awaitClose { childRef.removeEventListener(valueEventListener) }
    }



}