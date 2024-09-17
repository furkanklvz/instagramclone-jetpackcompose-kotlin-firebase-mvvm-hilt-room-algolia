package com.klavs.instagramclone.model

data class UserModel(
    val uid: String = "",
    val username: String = "",
    val name: String = "",
    val surname: String = "",
    val e_mail: String = "",
    val profile_picture_uri: String = "",
    val biography: String = "",
    val follower_list: MutableList<String>? = mutableListOf(),
    val following_list: MutableList<String>? = mutableListOf(),
    val posts: List<String>? = emptyList()
    )
