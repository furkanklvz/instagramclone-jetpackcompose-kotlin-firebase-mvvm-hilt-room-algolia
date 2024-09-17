package com.klavs.instagramclone.util

import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage


class FirebaseObject {
    companion object{
        val auth = Firebase.auth
        val db = Firebase.firestore
        val storage = Firebase.storage
    }
}