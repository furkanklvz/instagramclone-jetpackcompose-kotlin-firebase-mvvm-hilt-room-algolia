package com.klavs.instagramclone.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RecentSearchesDAO {

    @Query("SELECT * FROM recent_searches ORDER BY search_date DESC")
    suspend fun getAllRecentSearches(): List<RecentSearchesModel>

    @Query("SELECT * FROM recent_searches WHERE searched_by = :searched_by ORDER BY search_date DESC")
    suspend fun getRecentSearchesByUser(searched_by: String): List<RecentSearchesModel>

    @Query("DELETE FROM recent_searches WHERE user_id = :user_id")
    suspend fun deleteRecentSearch(user_id: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(recentSearch: RecentSearchesModel)


}