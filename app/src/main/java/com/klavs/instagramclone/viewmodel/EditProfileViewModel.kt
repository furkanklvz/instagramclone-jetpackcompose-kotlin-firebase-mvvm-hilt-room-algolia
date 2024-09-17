package com.klavs.instagramclone.viewmodel

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.util.FirebaseObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {

    var isLoading = mutableStateOf(false)
    var isUploaded = mutableStateOf(false)

    @SuppressLint("SuspiciousIndentation")
    fun SubmitAction(
        biography: String?="",
        profilePictureUri: ByteArray?
    ) {
        isUploaded.value = false
        isLoading.value = true


        if (auth.currentUser != null) {
            if (profilePictureUri != null) {
                val storageRef =
                    storage.reference.child("profile_pictures").child(auth.currentUser!!.uid)
                viewModelScope.launch(Dispatchers.IO) {
                val uploadTask = storageRef.putBytes(profilePictureUri)
                    uploadTask.continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let { throw it }
                        }
                        storageRef.downloadUrl
                    }.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val downloadUrl = task.result
                            val addedData = hashMapOf(
                                "biography" to biography,
                                "profile_picture_uri" to downloadUrl.toString()
                            )
                            db.collection("users").document(auth.currentUser!!.uid)
                                .update(addedData.toMap())
                                .addOnSuccessListener {
                                    auth.currentUser!!.updateProfile(userProfileChangeRequest {
                                        photoUri = downloadUrl
                                    }).addOnSuccessListener {

                                        isLoading.value = false
                                        println("Profil güncellemesi başarılı")
                                        isUploaded.value = true

                                    }.addOnFailureListener { e ->
                                            isLoading.value = false
                                            println("Profil güncellenirken hata: ${e.message}")
                                        }
                                }
                                .addOnFailureListener { e ->
                                    isLoading.value = false
                                    println("Firestore güncellenirken hata: ${e.message}")
                                }
                        }
                    }
                }

            } else {
                val addedData = hashMapOf(
                    "biography" to biography
                )
                viewModelScope.launch(Dispatchers.IO) {
                    db.collection("users").document(auth.currentUser!!.uid).set(addedData)
                        .addOnSuccessListener {
                            isLoading.value = false
                            println("yüklendi")
                            isUploaded.value = true
                        }

                }
            }
        }

    }
}