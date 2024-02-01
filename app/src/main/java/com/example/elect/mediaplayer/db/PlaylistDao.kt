package com.example.elect.mediaplayer.db

import androidx.lifecycle.LiveData
import androidx.room.*


@Dao
interface PlaylistDao {
    @Insert
    suspend fun createPlaylist(playlistEntity: PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongsToPlaylist(songEntities: List<SongEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediasToPlaylist(mediaEntities: List<MediaEntity>)

    @Query("SELECT * FROM PlaylistEntity WHERE playlist_name = :name")
    fun playlist(name: String): List<PlaylistEntity>

    @Transaction
    @Query("SELECT * FROM PlaylistEntity")
    suspend fun playlistsWithSongs(): List<PlaylistWithSongs>

    @Transaction
    @Query("SELECT * FROM PlaylistEntity")
    suspend fun playlistsWithMedias(): List<PlaylistWithMedias>

    @Query("SELECT * FROM PlaylistEntity")
    suspend fun playlists():List<PlaylistEntity>

    @Delete
    suspend fun deletePlaylists(playlistEntities: List<PlaylistEntity>)


    @Query("UPDATE PlaylistEntity SET playlist_name = :name WHERE playlist_id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, name: String)


    @Query("DELETE FROM SongEntity WHERE playlist_creator_id = :playlistId")
    suspend fun deletePlaylistSongs(playlistId: Long)

    @Query("DELETE FROM MediaEntity WHERE playlist_creator_id = :playlistId")
    suspend fun deletePlaylistMedias(playlistId: Long)

    @Transaction
    @Query("SELECT * FROM PlaylistEntity WHERE playlist_id = :playlistId")
    suspend fun playlistsWithSongs(playlistId: Long): PlaylistWithSongs

    @Query("SELECT * FROM SongEntity WHERE playlist_creator_id = :playlistId ORDER BY song_key asc")
    fun songsFromPlaylist(playlistId: Long): LiveData<List<SongEntity>>

    @Query("SELECT * FROM MediaEntity WHERE playlist_creator_id = :playlistId ORDER BY media_key asc")
    fun mediasFromPlaylist(playlistId: Long): LiveData<List<MediaEntity>>

    @Query("SELECT * FROM SongEntity WHERE playlist_creator_id = :playlistId ORDER BY song_key asc")
    suspend fun songsFromPlaylistId(playlistId: Long): List<SongEntity>

    @Query("SELECT * FROM MediaEntity WHERE playlist_creator_id = :playlistId ORDER BY media_key asc")
    suspend fun mediasFromPlaylistId(playlistId: Long): List<MediaEntity>

    @Query("DELETE FROM SongEntity WHERE playlist_creator_id = :playlistId AND id = :songId")
    suspend fun deleteSongFromPlaylist(playlistId: Long, songId: Long)

    @Query("DELETE FROM MediaEntity WHERE playlist_creator_id = :playlistId AND id = :songId")
    suspend fun deleteMediaFromPlaylist(playlistId: Long, songId: Long)


    @Query("SELECT EXISTS(SELECT * FROM MediaEntity WHERE playlist_creator_id = :playlistId)")
    fun checkPlaylistExists(playlistId: Long): LiveData<Boolean>
}