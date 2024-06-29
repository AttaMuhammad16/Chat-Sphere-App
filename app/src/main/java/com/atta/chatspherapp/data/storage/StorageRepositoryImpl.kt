package com.atta.chatspherapp.data.storage

import android.graphics.Bitmap
import android.net.Uri
import com.atta.chatspherapp.utils.MyResult
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.storage
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import javax.inject.Inject


class StorageRepositoryImpl @Inject constructor(private val storageReference: StorageReference) : StorageRepository {

    override suspend fun uploadImageToFirebaseStorage(uri: String): MyResult<String> {
        return try {
            val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
            val uploadTask = imageRef.putFile(Uri.parse(uri))
            val result: UploadTask.TaskSnapshot = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun uploadImageToFirebaseStorage(bitmap: Bitmap): MyResult<String> {
        return try {
            val byteArray = bitmapToByteArray(bitmap)
            val imageRef = storageReference.child("images/${System.currentTimeMillis()}.jpg")
            val uploadTask = imageRef.putBytes(byteArray)
            val result= uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun deleteDocumentToFirebaseStorage(url: String): MyResult<String> {
        return try {
            val storage = FirebaseStorage.getInstance()
            val storageRef = storage.getReferenceFromUrl(url)
            val deleteTask: Task<Void> = storageRef.delete()
            Tasks.await(deleteTask)
            MyResult.Success("Image deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
            MyResult.Error("Failed to delete image: ${e.message}")
        }
    }

    override suspend fun uploadVideoToFirebaseStorage(
        videoUri: Uri,
        progressCallBack: (Int) -> Unit,
        uploadtask: (UploadTask) -> Unit
    ): MyResult<String> {
        return try {
            val uploadTask = storageReference.child("videos/${System.currentTimeMillis()}.mp4").putFile(videoUri)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressCallBack(progress)
            }
            uploadtask.invoke(uploadTask)
            val result = uploadTask.await()
            val downloadUrl = result.storage.downloadUrl.await()
            MyResult.Success(downloadUrl.toString())
        } catch (e: Exception) {
            MyResult.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun uploadAudioToFirebase(uri: Uri): MyResult<String> {
        val fileName = "${System.currentTimeMillis()}.3gp"
        return try {
            val storageRef = Firebase.storage.reference.child("audios/$fileName")
            storageRef.putFile(uri).await()
            MyResult.Success(storageRef.downloadUrl.await().toString())
        }catch (e:Exception){
            MyResult.Error("")
        }
    }



    private suspend fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }




}