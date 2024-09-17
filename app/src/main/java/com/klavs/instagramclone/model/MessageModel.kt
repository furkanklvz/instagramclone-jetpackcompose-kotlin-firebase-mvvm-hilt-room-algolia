package com.klavs.instagramclone.model

import com.google.firebase.Timestamp
import java.util.Date

data class MessageModel(
    val message: String? = null,
    val sender: String? = null,
    val time: Long? = null,
    val readed: Boolean? = null,
    val messageID: String? = null,
    val chatID: String? = null
)