package com.atta.chatspherapp.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.atta.chatspherapp.data.auth.AuthRepository
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val authRepository: AuthRepository, private val storageRepository: StorageRepository, private val mainRepository: MainRepository):ViewModel() {


    fun <T> collectAnyModel(path: String, clazz: Class<T>, numberOfItems: Int = 0): Flow<List<T>>{
        return mainRepository.collectAnyModel(path, clazz, numberOfItems)
    }

    fun <T> collectAnyModel(path: String, clazz: Class<T>): Flow<List<T>>{
        return mainRepository.collectAnyModel(path,clazz)
    }

    suspend fun< T :Any> uploadAnyModel(path:String, model: T): MyResult<String>{
        return withContext(Dispatchers.IO){mainRepository.uploadAnyModel(path, model)}
    }

    suspend fun deleteAnyModel(path:String): MyResult<String>{
        return withContext(Dispatchers.IO){mainRepository.deleteAnyModel(path)}
    }

    suspend fun <T> getAnyData(path:String, clazz: Class<T>): T?{
        return withContext(Dispatchers.IO){mainRepository.getAnyData(path, clazz)}
    }

    suspend fun checkPhoneNumberExists(path: String,phoneNumber: String): MyResult<Boolean>{
        return withContext(Dispatchers.IO){mainRepository.checkPhoneNumberExists(path,phoneNumber)}
    }


}


