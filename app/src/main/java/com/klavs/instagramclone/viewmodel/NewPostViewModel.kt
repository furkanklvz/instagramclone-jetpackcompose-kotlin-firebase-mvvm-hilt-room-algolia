package com.klavs.instagramclone.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.model.ScreenModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class UploadingPostStatements {
    object Loading : UploadingPostStatements()
    object Success : UploadingPostStatements()
    object Idle : UploadingPostStatements()
    data class Error(val message: String? = "Unknown error") : UploadingPostStatements()
}

@HiltViewModel
class NewPostViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore
) : ViewModel() {
    var statement: MutableState<UploadingPostStatements> =
        mutableStateOf(UploadingPostStatements.Idle)


    fun UploadPost(navController: NavHostController, imageOfPost: ByteArray, description: String) {
        statement.value = UploadingPostStatements.Loading

        val postRef = db.collection("posts").document()

        val imageRef = storage.reference.child("posts").child(postRef.id)
        viewModelScope.launch(Dispatchers.IO) {
            imageRef.putBytes(imageOfPost).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener {
                    val postData = hashMapOf(
                        "post_owner" to auth.currentUser!!.uid,
                        "image_uri" to it,
                        "description" to description,
                        "date" to Timestamp.now(),
                        "like_list" to emptyList<String>(),
                        "comment_list" to emptyList<Map<String, Any>>()
                    )
                    postRef.set(postData).addOnSuccessListener {
                        val userRef = db.collection("users").document(auth.currentUser!!.uid)
                        userRef.update("posts", FieldValue.arrayUnion(postRef.id))
                            .addOnSuccessListener {
                                statement.value = UploadingPostStatements.Success
                                navController.navigate(ScreenModel.Home.route) {
                                    popUpTo(ScreenModel.Home.route) {
                                        inclusive = true
                                    }
                                }
                            }.addOnFailureListener {
                                statement.value = UploadingPostStatements.Error(it.localizedMessage)
                            }

                    }.addOnFailureListener {
                        statement.value = UploadingPostStatements.Error(it.localizedMessage)
                    }
                }.addOnFailureListener {
                    statement.value = UploadingPostStatements.Error(it.localizedMessage)
                }

            }.addOnFailureListener {
                statement.value = UploadingPostStatements.Error(it.localizedMessage)
            }
        }

    }
}