package com.klavs.instagramclone.util

import android.content.Context
import androidx.room.Room
import com.klavs.instagramclone.room.RecentSearchesDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RoomDatabaseModule {

    @Provides
    @Singleton
    fun ProvideDatabase(@ApplicationContext context: Context): RecentSearchesDatabase{
        return Room.databaseBuilder(
            context,
            RecentSearchesDatabase::class.java,
            "recent_searches"
        ).build()
    }

    @Provides
    @Singleton
    fun ProvideDao(database: RecentSearchesDatabase) = database.recentSearchesDao()


}