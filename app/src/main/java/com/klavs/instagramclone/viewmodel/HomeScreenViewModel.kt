package com.klavs.instagramclone.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.klavs.instagramclone.model.PostModel
import com.klavs.instagramclone.util.FirebaseObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class MainPageStatements(
    val data: MutableList<PostModel>? = null,
    val message: String? = null
) {
    object Loading : MainPageStatements()
    class Success(data: MutableList<PostModel>) : MainPageStatements(data)
    class Error(message: String) : MainPageStatements(message = message)
    object Empty : MainPageStatements()
}

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel() {
    val mainPageState: MutableState<MainPageStatements> = mutableStateOf(MainPageStatements.Loading)
    val post_list = mutableStateListOf<PostModel>()
    val post_size = mutableStateOf(0)
    val isLoading = mutableStateOf(false)
    val refreshdata = mutableStateListOf<PostModel>()


    fun GetPosts() {
        mainPageState.value = MainPageStatements.Loading
        refreshdata.clear()
        isLoading.value = true
        post_list.clear()
        if (auth.currentUser != null) {
            val userRef = db.collection("users")
                .document(auth.currentUser!!.uid)
            viewModelScope.launch(Dispatchers.IO) {
                userRef.get().addOnSuccessListener { user ->
                    if (user.exists() && user != null) {
                        val data = user.data
                        if (!data.isNullOrEmpty()) {
                            if (data.containsKey("following_list")) {
                                val following_list = data.get("following_list") as List<String>
                                if (following_list.isNullOrEmpty()) {
                                    mainPageState.value = MainPageStatements.Empty
                                } else {
                                    following_list.forEachIndexed { index, friend ->
                                        val postRef = db.collection("posts")
                                            .whereEqualTo("post_owner", friend)
                                        postRef.get().addOnSuccessListener { posts ->
                                            if (posts != null) {
                                                post_size.value += posts.size()
                                                posts.documents.forEachIndexed { indexOfPost, post ->
                                                    if (post.exists() && post != null) {
                                                        val post_model =
                                                            post.toObject(PostModel::class.java)
                                                        if (post_model != null) {
                                                            post_model.post_id = post.id
                                                            post_list.add(post_model)
                                                            refreshdata.add(post_model)
                                                            if (index == following_list.size - 1 && indexOfPost == posts.size() - 1) {
                                                                if (post_list.size == 0) {
                                                                    mainPageState.value =
                                                                        MainPageStatements.Empty
                                                                } else {
                                                                    post_list.sortByDescending { postModel ->
                                                                        postModel.date.toDate()
                                                                    }
                                                                    mainPageState.value =
                                                                        MainPageStatements.Success(
                                                                            post_list
                                                                        )
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }.addOnFailureListener {
                                            mainPageState.value =
                                                MainPageStatements.Error(it.message.toString())
                                        }
                                    }
                                }

                                mainPageState.value = MainPageStatements.Success(post_list)
                                isLoading.value = false
                            }

                        }
                    }
                }.addOnFailureListener {
                    mainPageState.value = MainPageStatements.Error(it.message.toString())
                }
            }
        }

    }
}