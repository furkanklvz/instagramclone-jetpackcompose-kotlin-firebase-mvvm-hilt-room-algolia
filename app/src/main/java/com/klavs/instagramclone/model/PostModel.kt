package com.klavs.instagramclone.model


import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.google.firebase.Timestamp
import java.time.Clock
import java.time.Instant

data class PostModel(
    var post_id: String = "",
    val post_owner: String = "",
    var post_owner_object: UserModel? = null,
    val image_uri: String = "",
    val description: String = "",
    val like_list: MutableList<String> = mutableListOf(),
    val comments: MutableList<CommentModel> = mutableListOf(),
    val date: Timestamp = Timestamp.now()
)