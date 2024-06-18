package com.atta.chatspherapp.ui.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.storage.UploadTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


@HiltViewModel
class StorageViewModel @Inject constructor(private val storageRepository: StorageRepository) : ViewModel() {

    // fire base storage
    suspend fun uploadImageToFirebaseStorage(uri: String): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorage(uri)}
    }

    suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.uploadImageToFirebaseStorage(bitmap)}
    }

    suspend fun uploadVideoToFirebaseStorage(videoUri: Uri, progressCallBack: (Int) -> Unit, uploadtask:(UploadTask)->Unit): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.uploadVideoToFirebaseStorage(videoUri, progressCallBack, uploadtask)}
    }

    suspend fun uploadAudioToFirebase(uri: Uri): MyResult<String>  {
        return withContext(Dispatchers.IO){storageRepository.uploadAudioToFirebase(uri)}
    }

    suspend fun deleteDocumentToFirebaseStorage(url: String): MyResult<String> {
        return withContext(Dispatchers.IO){storageRepository.deleteDocumentToFirebaseStorage(url)}
    }


}