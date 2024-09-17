package com.klavs.instagramclone.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recent_searches")
data class RecentSearchesModel(
    @PrimaryKey val user_id: String,
    val username: String,
    val search_date: String,
    val searched_by: String,
    val image: ByteArray
)
