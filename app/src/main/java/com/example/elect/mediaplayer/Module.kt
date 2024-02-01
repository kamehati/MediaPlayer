package com.example.elect.mediaplayer

import androidx.room.Room
import com.example.elect.mediaplayer.auto.AutoMusicProvider
import com.example.elect.mediaplayer.fragments.playlist.PlaylistDetailsViewModel
import com.example.elect.mediaplayer.db.MediaDatabase
import com.example.elect.mediaplayer.db.PlaylistWithMedias
import com.example.elect.mediaplayer.db.PlaylistWithSongs
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.music.album.AlbumDetailsViewModel
import com.example.elect.mediaplayer.fragments.music.artist.ArtistDetailsViewModel
import com.example.elect.mediaplayer.fragments.music.folder.SongFolderDetailsViewModel
import com.example.elect.mediaplayer.fragments.video.VideoFolderDetailsViewModel
import com.example.elect.mediaplayer.repository.*
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module


private val roomModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            MediaDatabase::class.java,
            "playlist.db"
        ).build()
    }

    factory {
        get<MediaDatabase>().playlistDao()
    }

    factory {
        get<MediaDatabase>().favoriteDao()
    }

    single {
        RealRoomRepository(
            get(),
            get()
        )
    } bind RoomRepository::class
}

private val autoModule = module {
    single {
        AutoMusicProvider(
            androidContext(),
            get()
        )
    }
}

private val dataModule = module {
    single {
        RealRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get()
        )
    } bind Repository::class

    single {
        RealSongRepository(
            get()
        )
    } bind SongRepository::class

    single {
        RealVideoRepository(
            get()
        )
    } bind VideoRepository::class

    single {
        RealMediaRepository(
            get()
        )
    } bind MediaRepository::class

    single {
        RealAlbumRepository(
            get(),
            get()
        )
    } bind AlbumRepository::class

    single {
        RealArtistRepository(
            get(),
            get()
        )
    } bind ArtistRepository::class
}

private val mainModule = module {
    single {

        androidContext().contentResolver
    }
}

private val viewModule = module {
    viewModel {
        LibraryViewModel(get())
    }

    viewModel { (playlist: PlaylistWithMedias) ->
        PlaylistDetailsViewModel(
            get(),
            playlist
        )
    }

    viewModel { (albumId: Long) ->
        AlbumDetailsViewModel(
            get(),
            albumId
        )
    }

    viewModel { (artistId: Long) ->
        ArtistDetailsViewModel(
            get(),
            artistId
        )
    }

    viewModel { (folderId: Long) ->
        SongFolderDetailsViewModel(
            get(),
            folderId
        )
    }

    viewModel { (folderId: Long) ->
        VideoFolderDetailsViewModel(
            get(),
            folderId
        )
    }
}

val appModule = listOf(
    roomModule,
    autoModule,
    dataModule,
    mainModule,
    viewModule
)