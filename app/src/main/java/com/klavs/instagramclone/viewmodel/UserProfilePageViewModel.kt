package com.klavs.instagramclone.viewmodel


import android.graphics.BitmapFactory
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.util.FirebaseObject
import com.klavs.instagramclone.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProfilePageStatements {
    object Loading : ProfilePageStatements()
    object Success : ProfilePageStatements()
    object Error : ProfilePageStatements()
}

sealed class ProfilePictureStatements {
    object Loading : ProfilePictureStatements()
    object Success : ProfilePictureStatements()
    object Error: ProfilePictureStatements()
}
sealed class PhotosStatements(val post_id: String?="", val message: String?="") {
    object Loading : PhotosStatements()
    class Success(post_id: String) : PhotosStatements(post_id = post_id)
    object Empty : PhotosStatements()
    class Error(message: String) : PhotosStatements(message = message)
}

@HiltViewModel
class UserProfilePageViewModel @Inject constructor(
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage,
    private val auth: FirebaseAuth
) : ViewModel() {
    var statement : MutableState<ProfilePageStatements> = mutableStateOf(ProfilePageStatements.Loading)
    var pictureStatement : MutableState<ProfilePictureStatements> = mutableStateOf(ProfilePictureStatements.Loading)
    var photosStatement : MutableState<PhotosStatements> = mutableStateOf(PhotosStatements.Loading)
    lateinit var user :UserModel
    var profilePicture : ImageBitmap? = null
    val photos = mutableStateMapOf<String,ImageBitmap>()


    fun Follow(user_id: String){
        val myRef = db.collection("users").document(auth.currentUser!!.uid)
        val userRef = db.collection("users").document(user_id)
        viewModelScope.launch(Dispatchers.IO) {
            myRef.update("following_list", FieldValue.arrayUnion(user_id))
            userRef.update("follower_list", FieldValue.arrayUnion(auth.currentUser!!.uid))
        }
        user.follower_list!!.add(auth.currentUser!!.uid)
    }
    fun Unfollow(user_id: String){
        val myRef = db.collection("users").document(auth.currentUser!!.uid)
        val userRef = db.collection("users").document(user_id)
        viewModelScope.launch(Dispatchers.IO) {
            myRef.update("following_list", FieldValue.arrayRemove(user_id))
            userRef.update("follower_list", FieldValue.arrayRemove(auth.currentUser!!.uid))
        }
        user.follower_list!!.remove(auth.currentUser!!.uid)
    }



    fun GetUserProfile(username: String){
        statement.value = ProfilePageStatements.Loading
        viewModelScope.launch(Dispatchers.IO) {
            db.collection("users").whereEqualTo("username",username).get().addOnSuccessListener {
                if(!it.isEmpty){
                    statement.value = ProfilePageStatements.Success
                    user = it.documents.get(0).toObject(UserModel::class.java)!!
                    GetPhotos(user.posts)
                }else{
                    statement.value = ProfilePageStatements.Error
                }
            }
        }

    }
    fun GetProfilePicture(uid: String){
        pictureStatement.value = ProfilePictureStatements.Loading
        val storageRef = storage.reference
        val imageRef = storageRef.child("profile_pictures/${uid}")
        viewModelScope.launch(Dispatchers.IO) {
            imageRef.getBytes(512 * 512).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                profilePicture = bitmap.asImageBitmap()
                pictureStatement.value = ProfilePictureStatements.Success
            }.addOnFailureListener {
                pictureStatement.value = ProfilePictureStatements.Error
            }
        }
    }
    fun GetPhotos(uriList: List<String>?) {
        photos.clear()
        if (uriList.isNullOrEmpty()) {
            photosStatement.value = PhotosStatements.Empty
            return
        } else {
            photosStatement.value = PhotosStatements.Loading
        }
        viewModelScope.launch(Dispatchers.IO) {
            uriList.forEach { uri ->
                val storageRef = storage.reference
                val imageRef = storageRef.child("posts/${uri}")
                imageRef.getBytes(1024 * 1024).addOnSuccessListener {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    photos.put(uri, bitmap.asImageBitmap())
                    if (photos.size == uriList.size) {
                        photosStatement.value = PhotosStatements.Success(uri)
                    }
                }
            }
        }
    }
}