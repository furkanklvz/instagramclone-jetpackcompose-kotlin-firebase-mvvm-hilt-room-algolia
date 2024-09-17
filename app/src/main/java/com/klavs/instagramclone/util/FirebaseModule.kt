package com.klavs.instagramclone.util


import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.database
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FirebaseModule {

    @Provides
    @Singleton
    fun ProvideFirebaseAuth(): FirebaseAuth{
        return Firebase.auth
    }
    @Provides
    @Singleton
    fun ProvideFirebaseFirestore(): FirebaseFirestore{
        return Firebase.firestore
    }
    @Provides
    @Singleton
    fun ProvideFirebaseStorage(): FirebaseStorage {
        return Firebase.storage
    }
    @Provides
    @Singleton
    fun ProvideFirebaseDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance("https://instagram-clone-c02e3-default-rtdb.europe-west1.firebasedatabase.app/")
    }

}