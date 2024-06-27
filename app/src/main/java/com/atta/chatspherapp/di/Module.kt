package com.atta.chatspherapp.di

import com.atta.chatspherapp.data.auth.AuthRepository
import com.atta.chatspherapp.data.auth.AuthRepositoryImpl
import com.atta.chatspherapp.data.auth.email.AuthRepositoryWithEmail
import com.atta.chatspherapp.data.auth.email.AuthRepositoryWithEmailImpl
import com.atta.chatspherapp.data.auth.phone.AuthRepositoryWithPhone
import com.atta.chatspherapp.data.main.MainRepository
import com.atta.chatspherapp.data.main.MainRepositoryImpl
import com.atta.chatspherapp.data.storage.StorageRepository
import com.atta.chatspherapp.data.storage.StorageRepositoryImpl
import com.atta.chatspherapp.ui.viewmodel.AuthViewModel
import com.atta.chatspherapp.ui.viewmodel.MainViewModel
import com.atta.chatspherapp.ui.viewmodel.StorageViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.atta.chatspherapp.data.auth.phone.AuthRepositoryWithPhoneImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
object Module {

    @Provides
    @Singleton
    fun provideMainRepo(databaseReference: DatabaseReference): MainRepository {
        return MainRepositoryImpl(databaseReference)
    }


    @Provides
    @Singleton
    fun provideMainViewModel(mainRepository: MainRepository,authRepository: AuthRepository,storageRepository: StorageRepository) : MainViewModel {
        return MainViewModel(authRepository,storageRepository,mainRepository)
    }



    @Provides
    @Singleton
    fun provideStorageViewModel(storageRepository: StorageRepository) : StorageViewModel {
        return StorageViewModel(storageRepository)
    }



    @Provides
    @Singleton
    fun provideAuthViewModel(authRepositoryWithEmail: AuthRepositoryWithEmail, authRepositoryWithPhone: AuthRepositoryWithPhone) : AuthViewModel {
        return AuthViewModel(authRepositoryWithEmail,authRepositoryWithPhone)
    }

    @Provides
    @Singleton
    fun provideFirebaseDataBase(): DatabaseReference {
        return FirebaseDatabase.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseStorageRef(): StorageReference {
        return FirebaseStorage.getInstance().reference
    }

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage {
        return FirebaseStorage.getInstance()
    }


    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }


    @Provides
    @Singleton
    fun provideAuthRepositoryWithPhone(auth: FirebaseAuth): AuthRepositoryWithPhone {
        return AuthRepositoryWithPhoneImpl(auth)
    }


    @Provides
    @Singleton
    fun provideAuthRepositoryWithEmail(auth: FirebaseAuth) : AuthRepositoryWithEmail {
        return AuthRepositoryWithEmailImpl(auth)
    }


    @Provides
    @Singleton
    fun provideStorageRepository(storageReference: StorageReference): StorageRepository {
       return StorageRepositoryImpl(storageReference)
    }




    @Provides
    @Singleton
    fun provideAuthRepository(auth: FirebaseAuth): AuthRepository {
        return AuthRepositoryImpl(auth)
    }




}