package com.klavs.instagramclone.viewmodel

import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.model.CommentModel
import com.klavs.instagramclone.model.PostModel
import com.klavs.instagramclone.model.UserModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PostScreenStatements(
    val postData: PostModel? = null,
    val errorMessage: String? = null
) {
    object Loading : PostScreenStatements()
    class Success(data: PostModel) : PostScreenStatements(data)
    class Error(message: String) : PostScreenStatements(errorMessage = message)
}

sealed class ImageStatements(val image: ImageBitmap? = null, val errorMessage: String? = null) {
    object Loading : ImageStatements()
    class Success(image: ImageBitmap) : ImageStatements(image = image)
    class Error(message: String) : ImageStatements(errorMessage = message)
}

@HiltViewModel
class PostScreenViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val db: FirebaseFirestore
) : ViewModel() {

    // İzole edilmiş post_id bazlı durumlar için MutableStateMap
    var postStatementMap = mutableStateMapOf<String, MutableState<PostScreenStatements>>()
    var postImageStatementMap = mutableStateMapOf<String, MutableState<ImageStatements>>()
    var isPostLikedMap = mutableStateMapOf<String, MutableState<Boolean>>()
    var isCommentLikedMap = mutableStateMapOf<String, MutableState<Boolean>>()
    var profilePictureMap = mutableStateMapOf<String, ByteArray?>()
    var commentListMap = mutableStateMapOf<String, MutableList<CommentModel>>()
    val comment_profile_pictures = mutableStateMapOf<String, ByteArray>()
    val post_object_list = mutableStateListOf<PostModel>()


    fun Comment(postID: String, comment: String) {
        val postRef = db.collection("posts").document(postID)
        val comment_id = auth.currentUser!!.uid + Timestamp.now().toString()
        val newComment = hashMapOf(
            "comment_id" to comment_id,
            "user_id" to auth.currentUser!!.uid,
            "comment" to comment,
            "date" to Timestamp.now(),
            "like_list" to mutableListOf<String>(),
            "username" to auth.currentUser!!.displayName!!,
            "post_id" to postID
        )
        viewModelScope.launch(Dispatchers.IO) {
            postRef.collection("comments").document(comment_id).set(newComment)
                .addOnSuccessListener {
                    commentListMap[postID]?.add(
                        CommentModel(
                            comment_id,
                            auth.currentUser!!.uid,
                            comment,
                            Timestamp.now(),
                            mutableListOf(),
                            auth.currentUser!!.displayName!!,
                            postID
                        )
                    )


                }.addOnFailureListener {
                println("Yorum yapılırken hata oluşyu: " + it.localizedMessage)
            }
        }


    }



    fun GetCommentProfilePicture(user_id: String) {
        if (!comment_profile_pictures.containsKey(user_id)) {
            val imageRef = storage.reference.child("profile_pictures/${user_id}")
            viewModelScope.launch(Dispatchers.IO) {
                imageRef.getBytes(512 * 512).addOnSuccessListener {
                    comment_profile_pictures[user_id] = it
                }
            }
        }
    }

    fun LikeThePost(postID: String, like: Boolean) {
        val postRef = db.collection("posts").document(postID)
        if (like) {
            isPostLikedMap[postID]?.value = true
            post_object_list.toList().find { it.post_id == postID }?.let {
                it.like_list.add(auth.currentUser!!.uid)
            }
            viewModelScope.launch(Dispatchers.IO) {
                postRef.update("like_list", FieldValue.arrayUnion(auth.currentUser!!.uid))
            }
        } else {
            isPostLikedMap[postID]?.value = false
            viewModelScope.launch(Dispatchers.IO) {
                postRef.update("like_list", FieldValue.arrayRemove(auth.currentUser!!.uid))
            }
            post_object_list.toList().find { it.post_id == postID }?.let {
                it.like_list.remove(auth.currentUser!!.uid)
            }
        }
    }
    fun LikeTheComment(comment_id: String, postID: String, like: Boolean) {
        if (post_object_list.toList().find { it.post_id == postID } != null) {
            val commentRef =
                db.collection("posts").document(postID).collection("comments").document(comment_id)
            if (like) {
                viewModelScope.launch(Dispatchers.IO) {
                    commentRef.update("like_list", FieldValue.arrayUnion(auth.currentUser!!.uid))
                }
                commentListMap.get(postID)?.find { it.comment_id == comment_id }?.like_list?.add(auth.currentUser!!.uid)
            } else {
                viewModelScope.launch(Dispatchers.IO) {
                    commentRef.update("like_list", FieldValue.arrayRemove(auth.currentUser!!.uid))
                }
                commentListMap.get(postID)?.find { it.comment_id == comment_id }?.like_list?.remove(auth.currentUser!!.uid)
            }

        }
    }

    fun GetPost(postID: String) {
        // İzole edilmiş her post_id için yeni bir durum başlatıyoruz
        postStatementMap[postID] = mutableStateOf(PostScreenStatements.Loading)
        postImageStatementMap[postID] = mutableStateOf(ImageStatements.Loading)
        isPostLikedMap[postID] = mutableStateOf(false)
        commentListMap[postID] = mutableStateListOf()

        viewModelScope.launch(Dispatchers.IO) {
            val docRef = db.collection("posts").document(postID)
            docRef.get().addOnSuccessListener {
                val post = it.toObject(PostModel::class.java)
                if (post != null) {
                    post.post_id = postID
                    if (post.like_list.contains(auth.currentUser!!.uid)) {
                        isPostLikedMap[postID]?.value = true
                    }
                    GetUser(post.post_owner) { user ->
                        post.post_owner_object = user
                        GetComment(postID) {
                            commentListMap.get(postID)?.clear()
                            it.documents.forEach { comment ->
                                val comment_object = comment.toObject(CommentModel::class.java)
                                if (comment_object != null) {
                                    post.comments.add(comment_object)
                                    commentListMap[postID]?.add(comment_object)
                                }
                            }
                        }
                        postStatementMap[postID]?.value = PostScreenStatements.Success(post)
                        post_object_list.add(post)
                        GetImage(postID)
                    }
                }
            }.addOnFailureListener {
                postStatementMap[postID]?.value = PostScreenStatements.Error(it.localizedMessage)
            }
        }
    }

    fun GetComment(post_id: String, callback: (QuerySnapshot) -> Unit) {
        val postRef = db.collection("posts").document(post_id)
        viewModelScope.launch(Dispatchers.IO) {
            postRef.collection("comments").get().addOnSuccessListener {
                if (it != null) {
                    callback(it)
                }
            }
        }
    }

    private fun GetUser(uid: String, callback: (UserModel) -> Unit) {
        val docRef = db.collection("users").document(uid)
        viewModelScope.launch(Dispatchers.IO) {
            docRef.get().addOnSuccessListener {
                val user = it.toObject(UserModel::class.java)
                if (user != null) {
                    GetProfilePicture(user.uid)
                    callback(user)
                }
            }
        }
    }

    private fun GetImage(postID: String) {
        postImageStatementMap[postID]?.value = ImageStatements.Loading
        val imageRef = storage.reference.child("posts/$postID")
        viewModelScope.launch(Dispatchers.IO) {
            imageRef.getBytes(1920 * 1080).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                postImageStatementMap[postID]?.value =
                    ImageStatements.Success(bitmap.asImageBitmap())
            }.addOnFailureListener {
                postImageStatementMap[postID]?.value = ImageStatements.Error(it.localizedMessage)
            }
        }
    }

    private fun GetProfilePicture(uid: String) {
        if (!profilePictureMap.containsKey(uid)) {
            val imageRef = storage.reference.child("profile_pictures/$uid")
            viewModelScope.launch(Dispatchers.IO) {
                imageRef.getBytes(512 * 512).addOnSuccessListener {
                    profilePictureMap.put(uid, it)
                }
            }
        }
    }
}


