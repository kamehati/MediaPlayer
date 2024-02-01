package com.example.elect.mediaplayer.db

import androidx.room.*
import com.example.elect.mediaplayer.model.Media

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteEntity(favoriteEntity: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaFavoriteEntity(mediaFavoriteEntity: MediaFavoriteEntity)

    @Query("SELECT * FROM FavoriteEntity WHERE song_is_favorite = :isFavorite")
    suspend fun favoriteEntities(isFavorite: Boolean = true): List<FavoriteEntity>

    @Query("SELECT * FROM MediaFavoriteEntity WHERE media_is_favorite = :isFavorite")
    suspend fun mediaFavoriteEntities(isFavorite: Boolean = true): List<MediaFavoriteEntity>

    @Query("SELECT DISTINCT * FROM FavoriteEntity WHERE song_id = :songId")
    suspend fun favoriteEntity(songId: Long): List<FavoriteEntity>


    @Query("SELECT DISTINCT * FROM MediaFavoriteEntity WHERE media_id = :mediaId")
    suspend fun mediaFavoriteEntity(mediaId: Long): List<MediaFavoriteEntity>


    @Query("SELECT DISTINCT * FROM MediaFavoriteEntity WHERE media_id = :mediaId")
    fun mediaFavorite(mediaId: Long): List<MediaFavoriteEntity>

    @Query("UPDATE FavoriteEntity SET song_is_favorite = :isFavorite WHERE song_id = :songId")
    suspend fun updateFavoriteEntity(songId: Long, isFavorite: Boolean)

    @Query("UPDATE MediaFavoriteEntity SET media_is_favorite = :isFavorite WHERE media_id = :mediaId")
    suspend fun updateMediaFavoriteEntity(mediaId: Long, isFavorite: Boolean)

    @Transaction
    suspend fun insertOrUpdateFavoriteEntity(
        songId: Long,
        isFavorite: Boolean,
        favoriteEntity: FavoriteEntity
    ) {

        val itemFromDB = favoriteEntity(songId)

        if(itemFromDB.isEmpty()){

            insertFavoriteEntity(favoriteEntity)
        }

        else {

            updateFavoriteEntity(songId, isFavorite)
        }
    }

    @Transaction
    suspend fun insertOrUpdateMediaFavoriteEntity(
        mediaId: Long,
        isFavorite: Boolean,
        mediaFavoriteEntity: MediaFavoriteEntity
    ) {

        val itemFromDB = mediaFavoriteEntity(mediaId)

        if(itemFromDB.isEmpty()){

            insertMediaFavoriteEntity(mediaFavoriteEntity)
        }

        else {

            updateMediaFavoriteEntity(mediaId, isFavorite)
        }
    }

    @Transaction
    suspend fun isMediaFavoriteEntity(mediaId: Long): Boolean{

        val itemFromDB = mediaFavoriteEntity(mediaId)

        if(itemFromDB.isNotEmpty()){

            for (item in itemFromDB){

                if(item.id == mediaId){

                    if(item.isFavorite){
                        return true
                    }
                }
            }

            return false
        }

        return false
    }

    fun isMediaFavorite(mediaId: Long): Boolean{

        val itemFromDB = mediaFavorite(mediaId)

        if(itemFromDB.isNotEmpty()){

            for (item in itemFromDB){

                if(item.id == mediaId){

                    if(item.isFavorite){
                        return true
                    }
                }
            }

            return false
        }

        return false
    }
}