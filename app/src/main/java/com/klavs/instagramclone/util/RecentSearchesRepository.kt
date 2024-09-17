package com.klavs.instagramclone.util

import com.klavs.instagramclone.room.RecentSearchesDAO
import com.klavs.instagramclone.room.RecentSearchesModel
import javax.inject.Inject

class RecentSearchesRepository @Inject constructor(private val dao: RecentSearchesDAO) {

    suspend fun getAllRecentSearches() = dao.getAllRecentSearches()
    suspend fun getRecentSearchesByUser(user_id: String) = dao.getRecentSearchesByUser(user_id)
    suspend fun deleteRecentSearch(user_id: Int) = dao.deleteRecentSearch(user_id)
    suspend fun insertRecentSearch(recentSearch: RecentSearchesModel) = dao.insertRecentSearch(recentSearch)
}