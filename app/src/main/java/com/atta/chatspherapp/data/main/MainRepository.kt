package com.atta.chatspherapp.data.main

import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.MyResult
import kotlinx.coroutines.flow.Flow

// new updated
interface MainRepository {

    fun <T> collectAnyModel(path: String, clazz: Class<T>, numberOfItems: Int = 0): Flow<List<T>>
    fun <T> collectAnyModel(path: String, clazz: Class<T>): Flow<List<T>>
    suspend fun< T :Any> uploadAnyModel(path:String, model: T): MyResult<String>
    suspend fun deleteAnyModel(path:String): MyResult<String>
    suspend fun <T> getAnyData(path:String, clazz: Class<T>): T?
    suspend fun checkPhoneNumberExists(path: String, phoneNumber: String): MyResult<Boolean>
    suspend fun updateNumberOfMessages(path: String): MyResult<String>
    suspend fun uploadMap(path: String, dataMap: HashMap<String,Any>): MyResult<Boolean>
    suspend fun getAnyModelFlow(path: String,userModel: UserModel): Flow<UserModel>
    suspend fun <T> getModelsList(path: String,clazz: Class<T>): MyResult<List<T>>

}
