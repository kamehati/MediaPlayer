package com.example.elect.mediaplayer.db

import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song

fun Song.toSongEntity(
    playlistId: Long
): SongEntity{
    return SongEntity(
        playlistCreatorId = playlistId,

        id = id,
        title = title,
        trackNumber = trackNumber,
        year = year,
        duration = duration,
        data = data,
        dateModified = dateModified,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        folderId = folderId,
        folderName = folderName,
        composer = composer,
        albumArtist = albumArtist,
    )
}


fun List<Song>.toSongEntity(
    playlistEntity: PlaylistEntity
): List<SongEntity>{

    return map {

        it.toSongEntity(
            playlistEntity.playlistId,
        )
    }
}


fun List<SongEntity>.toSongs(): List<Song> {
    return map {
        it.toSong()
    }
}

fun SongEntity.toSong(): Song {
    return Song(
        id = id,
        title = title,
        trackNumber = trackNumber,
        year = year,
        duration = duration,
        data = data,
        dateModified = dateModified,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        folderId = folderId,
        folderName = folderName,
        composer = composer,
        albumArtist = albumArtist
    )
}

fun Song.toFavoriteEntity(
    isFavorite: Boolean
): FavoriteEntity{
    return FavoriteEntity(
        id = this.id,
        title = this.title,
        trackNumber = this.trackNumber,
        year = this.year,
        duration = this.duration,
        data = this.data,
        dateModified = this.dateModified,
        albumId = this.albumId,
        albumName = this.albumName,
        artistId = this.artistId,
        artistName = this.artistName,
        folderId = this.folderId,
        folderName = this.folderName,
        composer = this.composer,
        albumArtist = this.albumArtist,
        isFavorite = isFavorite
    )
}

fun Media.toMediaFavoriteEntity(
    isFavorite: Boolean
): MediaFavoriteEntity{
    return MediaFavoriteEntity(
        id = this.id,
        title = this.title,
        year = this.year,
        duration = this.duration,
        data = this.data,
        dateModified = this.dateModified,
        folderId = this.folderId,
        folderName = this.folderName,

        songId = songId,
        trackNumber = this.trackNumber,
        albumId = this.albumId,
        albumName = this.albumName,
        artistId = this.artistId,
        artistName = this.artistName,
        composer = this.composer,
        albumArtist = this.albumArtist,

        videoId = this.videoId,
        size = this.size,

        isSongOrVideo = isSongOrVideo,

        isFavorite = isFavorite
    )
}

fun FavoriteEntity.toSong(): Song {
    return Song(
        id = this.id,
        title = this.title,
        trackNumber = this.trackNumber,
        year = this.year,
        duration = this.duration,
        data = this.data,
        dateModified = this.dateModified,
        albumId = this.albumId,
        albumName = this.albumName,
        artistId = this.artistId,
        artistName = this.artistName,
        folderId = this.folderId,
        folderName = this.folderName,
        composer = this.composer,
        albumArtist = this.albumArtist
    )
}


@JvmName("toSongsFavoriteEntity")
fun List<FavoriteEntity>.toSongs(): List<Song> {
    return map {
        it.toSong()
    }
}

fun Media.toMediaEntity(
    playlistId: Long
): MediaEntity{
    return MediaEntity(
        playlistCreatorId = playlistId,

        id = id,
        title = title,
        year = year,
        duration = duration,
        data = data,
        dateModified = dateModified,
        folderId = folderId,
        folderName = folderName,

        songId = songId,
        trackNumber = trackNumber,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        composer = composer,
        albumArtist = albumArtist,

        videoId = videoId,
        size = size,

        isSongOrVideo = isSongOrVideo
    )
}


fun List<Media>.toMediaEntity(
    playlistEntity: PlaylistEntity
): List<MediaEntity>{

    return map {

        it.toMediaEntity(
            playlistEntity.playlistId,
        )
    }
}

fun List<MediaEntity>.toMedias(): List<Media> {
    return map {
        it.toMedia()
    }
}

fun MediaEntity.toMedia(): Media {
    return Media(
        id =  id,
        title =  title,
        year =  year,
        duration = duration,
        data =  data,
        dateModified =  dateModified,
        folderId = folderId,
        folderName = folderName,

        songId = songId,
        trackNumber = trackNumber,
        albumId = albumId,
        albumName = albumName,
        artistId = artistId,
        artistName = artistName,
        composer = composer,
        albumArtist = albumArtist,

        videoId = videoId,
        size = size,

        isSongOrVideo = isSongOrVideo
    )
}


@JvmName("toMediasFavoriteEntity")

fun List<MediaFavoriteEntity>.toMedias(): List<Media> {
    return map {
        it.toMedia()
    }
}

fun MediaFavoriteEntity.toMedia(): Media {
    return Media(
        id =  id,
        title =  title,
        year =  year,
        duration =  duration,
        data =  data,
        dateModified =  dateModified,
        folderId =  folderId,
        folderName =  folderName,

        songId =  songId,
        trackNumber =  trackNumber,
        albumId =  albumId,
        albumName =  albumName,
        artistId =  artistId,
        artistName =  artistName,
        composer =  composer,
        albumArtist =  albumArtist,

        videoId =  videoId,
        size =  size,

        isSongOrVideo =  isSongOrVideo
    )
}