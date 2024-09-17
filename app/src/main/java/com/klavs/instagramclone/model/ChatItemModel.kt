package com.klavs.instagramclone.model

import java.util.Date

data class ChatItemModel(
    val chatID:String?=null,
    val messagingUserID:String?=null,
    val messagingUsername:String?=null,
    val lastMessage:String?=null,
    val lastMessageDate:Date?=null,
    val lastMessageSender:String?=null
)
