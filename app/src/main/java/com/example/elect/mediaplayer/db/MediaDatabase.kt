package com.example.elect.mediaplayer.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        PlaylistEntity::class,
        SongEntity::class,
        FavoriteEntity::class,
        MediaEntity::class,
        MediaFavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)


abstract class MediaDatabase: RoomDatabase() {
    abstract fun playlistDao(): PlaylistDao
    abstract fun favoriteDao(): FavoriteDao
}