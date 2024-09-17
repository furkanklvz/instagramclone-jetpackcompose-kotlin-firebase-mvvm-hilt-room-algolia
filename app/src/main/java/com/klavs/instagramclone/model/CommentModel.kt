package com.klavs.instagramclone.model

import com.google.firebase.Timestamp

data class CommentModel(
    val comment_id: String = "",
    val user_id: String = "",
    val comment: String = "",
    val date: Timestamp = Timestamp.now(),
    val like_list: MutableList<String> = mutableListOf(),
    val username: String = "",
    val post_id: String = ""
)
