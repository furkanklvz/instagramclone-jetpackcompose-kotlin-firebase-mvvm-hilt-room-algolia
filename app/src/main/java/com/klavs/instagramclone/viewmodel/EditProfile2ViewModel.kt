package com.klavs.instagramclone.viewmodel

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfileEditState(val data: UserModel? = null, val message: String? = null) {
    class Success(data: UserModel) : ProfileEditState(data)
    class Error(message: String) : ProfileEditState(message = message)
    object Loading : ProfileEditState()
}
sealed class ProfileUpdateState(val message: String? = null) {
    class Error(message: String) : ProfileUpdateState(message = message)
    object Loading : ProfileUpdateState()
    object Idle: ProfileUpdateState()
}

@HiltViewModel
class EditProfile2ViewModel @Inject constructor(
    val auth: FirebaseAuth,
    val db: FirebaseFirestore,
    val storage: FirebaseStorage
) : ViewModel() {

    val profileEditState: MutableState<ProfileEditState> = mutableStateOf(ProfileEditState.Loading)
    val isPictureDownloaded: MutableState<Boolean> = mutableStateOf(false)
    val profilePicture: MutableState<ByteArray?> = mutableStateOf(null)
    val profileUpdateState: MutableState<ProfileUpdateState> = mutableStateOf(ProfileUpdateState.Idle)

    init {
            if (auth.currentUser != null) {
                GetProfile(auth.currentUser!!.uid)
            }
    }


    fun GetProfile(uid: String) {
        profileEditState.value = ProfileEditState.Loading
        GetProfilePicture(uid)
        viewModelScope.launch(Dispatchers.IO) {
            val userRef = db.collection("users").document(uid)
            userRef.get().addOnSuccessListener {
                if (it.exists()) {
                    val user = it.toObject(UserModel::class.java)
                    if (user != null) {
                        profileEditState.value = ProfileEditState.Success(user)
                    }
                }
            }
        }

    }

    fun GetProfilePicture(uid: String) {
        isPictureDownloaded.value = false
        viewModelScope.launch(Dispatchers.IO) {
            val pictureRef = storage.reference.child("profile_pictures").child(uid)
            pictureRef.getBytes(512 * 512).addOnSuccessListener {
                profilePicture.value = it
                isPictureDownloaded.value = true
            }.addOnFailureListener {
                profilePicture.value = null
                isPictureDownloaded.value = true
            }
        }
    }
    fun UpdateProfilePicture(imageByteArray: ByteArray){
        if (auth.currentUser != null){
            val imageRef = storage.reference.child("profile_pictures").child(auth.currentUser!!.uid)
            viewModelScope.launch(Dispatchers.IO) {
                imageRef.putBytes(imageByteArray).addOnSuccessListener {
                    imageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                        auth.currentUser!!.updateProfile(userProfileChangeRequest {
                            photoUri = downloadUrl
                        }).addOnSuccessListener {
                            GetProfile(auth.currentUser!!.uid)
                            val userRef = db.collection("users").document(auth.currentUser!!.uid)
                            userRef.update("profile_picture_uri", downloadUrl)
                        }
                    }
                }
            }
        }
    }
    fun UpdateBiography(biography: String) {
        profileUpdateState.value = ProfileUpdateState.Loading
        if (auth.currentUser != null) {
            val userRef = db.collection("users").document(auth.currentUser!!.uid)
            viewModelScope.launch(Dispatchers.IO) {
                userRef.update("biography", biography).addOnSuccessListener {
                    profileUpdateState.value = ProfileUpdateState.Idle
                }.addOnFailureListener {
                    profileUpdateState.value =
                        ProfileUpdateState.Error("Güncellenemedi: " + it.localizedMessage)
                }
            }
        }
    }

    fun UpdateUsername(username: String){
        profileUpdateState.value = ProfileUpdateState.Loading
        if (auth.currentUser != null) {
            val userRef = db.collection("users").document(auth.currentUser!!.uid)
            viewModelScope.launch(Dispatchers.IO) {
                userRef.update("username", username).addOnSuccessListener {
                    auth.currentUser!!.updateProfile(userProfileChangeRequest {
                        displayName = username
                    })
                    profileUpdateState.value = ProfileUpdateState.Idle
                }.addOnFailureListener {
                    profileUpdateState.value =
                        ProfileUpdateState.Error("Güncellenemedi: " + it.localizedMessage)
                }
            }
        }
    }
}