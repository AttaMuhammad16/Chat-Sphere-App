package com.atta.chatspherapp.data.storage

import android.graphics.Bitmap
import android.net.Uri
import com.atta.chatspherapp.utils.MyResult
import com.google.firebase.storage.UploadTask

interface StorageRepository {

    //image storage
    suspend fun uploadImageToFirebaseStorage(uri: String): MyResult<String>
    suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap): MyResult<String>
    suspend fun uploadVideoToFirebaseStorage(videoUri: Uri, progressCallBack: (Int) -> Unit, uploadtask:(UploadTask)->Unit): MyResult<String>

    suspend fun deleteDocumentToFirebaseStorage(url: String): MyResult<String>
    suspend fun uploadAudioToFirebase(uri: Uri): MyResult<String>




}