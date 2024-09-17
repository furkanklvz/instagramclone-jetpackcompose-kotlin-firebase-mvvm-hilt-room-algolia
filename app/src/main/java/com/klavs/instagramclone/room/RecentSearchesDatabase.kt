package com.klavs.instagramclone.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentSearchesModel::class], version = 2)
abstract class RecentSearchesDatabase: RoomDatabase() {
    abstract fun recentSearchesDao(): RecentSearchesDAO

    companion object {
        @Volatile
        private var INSTANCE: RecentSearchesDatabase? = null

        fun getDatabase(context: Context): RecentSearchesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RecentSearchesDatabase::class.java,
                    "recent_searches_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}