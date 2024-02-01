package com.example.elect.mediaplayer.fragments.base

import android.content.Intent
import android.view.MenuItem
import androidx.annotation.LayoutRes
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.service.MusicService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BasePlayerFragment(
    @LayoutRes layout: Int
) : BaseActivityFragment(layout),
    Toolbar.OnMenuItemClickListener{

    abstract fun onShow()

    abstract fun onHide()

    abstract fun onBackPressed(): Boolean

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val media = PlayerRemote.currentMedia

        return when(item.itemId) {
            R.id.action_toggle_favorite -> {
                toggleFavorite(media)
                true
            }

            else -> true
        }
    }

    protected open fun toggleFavorite(media: Media) {

        lifecycleScope.launch(Dispatchers.IO) {


            val isFavorite = libraryViewModel.isMediaFavoriteEntity(media.id)

            libraryViewModel.apply {

                insertOrUpdateMediaFavoriteEntity(media, !isFavorite)

                forceReload(ReloadType.Favorites)
                forceReload(ReloadType.FavoriteMedias)
                forceReload(ReloadType.Songs)
                forceReload(ReloadType.Playlists)
            }

            withContext(Dispatchers.Main){
                val icon = if(!isFavorite){
                    GlideExtensions.DEFAULT_FAVORITE_TRUE
                }else {
                    GlideExtensions.DEFAULT_FAVORITE_FALSE
                }

                setImageResource(icon)
            }


            requireContext().sendBroadcast(
                Intent(MusicService.FAVORITE_STATE_CHANGED)
            )
        }
    }

    abstract fun setImageResource(icon: Int)
}