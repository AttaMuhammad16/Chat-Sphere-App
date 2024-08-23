package com.atta.chatspherapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.models.RecentChatModel
import com.atta.chatspherapp.models.UserModel
import com.atta.chatspherapp.utils.MyResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.annotation.meta.When
import javax.inject.Inject


@HiltViewModel
class MainViewModel @Inject constructor(private val storageRepository: StorageRepository, private val mainRepository: MainRepository):ViewModel() {
    var isRecentChatUploaded:MutableStateFlow<Boolean> = MutableStateFlow(false)
    var recentChatModel:MutableStateFlow<RecentChatModel> = MutableStateFlow(RecentChatModel())

    private var _isUserInActivity:MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isUserInActivity:StateFlow<Boolean> = _isUserInActivity

    private var _userFlow:MutableStateFlow<UserModel> = MutableStateFlow(UserModel())
    val userFlow:StateFlow<UserModel> = _userFlow

    private var _selectedItemFlow : MutableStateFlow<MutableList<RecentChatModel>> = MutableStateFlow(mutableListOf())
    val selectedItemFlow : StateFlow<List<RecentChatModel>> = _selectedItemFlow

    var changesProfileUrl=""
    var changedName=""


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

    suspend fun updateNumberOfMessages(path: String): MyResult<String>{
        return withContext(Dispatchers.IO){mainRepository.updateNumberOfMessages(path)}
    }

    suspend fun uploadMap(path: String, dataMap: HashMap<String,Any>): MyResult<Boolean>{
        return withContext(Dispatchers.IO){mainRepository.uploadMap(path, dataMap)}
    }

    suspend fun <T> getAnyModelFlow(path: String, clazz: Class<T>) {
        val modelFlow = mainRepository.getAnyModelFlow(path, clazz)

        modelFlow.collect { model ->
            when (model) {
                is RecentChatModel -> {
                    recentChatModel.value=model
                }
                is UserModel -> {
                    _isUserInActivity.value = model.activityState
                    _userFlow.value = model
                }
                else -> {
                    throw IllegalArgumentException("Unknown model type")
                }
            }
        }
    }


    suspend fun <T> getModelFlow(path: String,clazz:Class<T>):Flow<T>{
        val model=mainRepository.getAnyModelFlow(path, clazz)
        return model
    }


    fun addToSelectedList(recentChatModel: RecentChatModel,isAddedOrRemoved:(Boolean)->Unit){
        val selectedList=_selectedItemFlow.value
        if (selectedList.contains(recentChatModel)){
            selectedList.remove(recentChatModel)
            isAddedOrRemoved.invoke(false)
        }else{
            selectedList.add(recentChatModel)
            isAddedOrRemoved.invoke(true)
        }
        _selectedItemFlow.value=selectedList
    }

    fun clearSelectedItemsList(){
        _selectedItemFlow.value.clear()
    }


}


