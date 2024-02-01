package com.example.elect.mediaplayer.fragments.player

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.FragmentSimplePlayerBinding
import com.example.elect.mediaplayer.extensions.backgroundColor
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.*
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SimplePlayerFragment:
    BasePlayerFragment(R.layout.fragment_simple_player) {

    private var _binding: FragmentSimplePlayerBinding? = null
    private val binding get() = _binding!!

    private var controlsFragment: SimpleActionControlsFragment? = null

    override fun onShow() {
        controlsFragment?.show()
    }

    override fun onHide() {
        controlsFragment?.hide()

        onBackPressed()
    }

    override fun onBackPressed(): Boolean {
        return false
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentSimplePlayerBinding.bind(view)

        setColorGradient()

        setUpSubFragments()

        setUpPlayerToolbar()
    }

    private fun setColorGradient() {
        binding.colorGradientBackground.apply {
            visibility = View.VISIBLE
            backgroundColor(R.color.md_grey_800)
        }
    }

    private fun setUpSubFragments() {
        controlsFragment = childFragmentManager
            .findFragmentById(
                R.id.simpleActionControlsFragment
            ) as SimpleActionControlsFragment
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_simple_player)
        binding.playerToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.playerToolbar.setOnMenuItemClickListener(this)

        binding.playerToolbar.backgroundColor(R.color.md_grey_800)
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        setIsFavorite()
    }

    override fun onPlayStateChanged() {
        super.onPlayStateChanged()
        setIsFavorite()
    }

    override fun onPlayingMetaChanged() {
        super.onPlayingMetaChanged()
        setIsFavorite()
    }

    override fun onFavoriteStateChanged() {
        super.onFavoriteStateChanged()
        setIsFavorite()
    }

    private fun setIsFavorite() {
        CoroutineScope(Dispatchers.IO).launch {
            val isFavorite = libraryViewModel.isMediaFavoriteEntity(PlayerRemote.currentMedia.id)
            val icon = if(isFavorite){
                GlideExtensions.DEFAULT_FAVORITE_TRUE
            }else {
                GlideExtensions.DEFAULT_FAVORITE_FALSE
            }
            withContext(Dispatchers.Main){
                setImageResource(icon)
            }
        }
    }

    override fun setImageResource(icon: Int) {
        binding.playerToolbar.menu?.findItem(
            R.id.action_toggle_favorite
        )?.apply {
            setIcon(icon)
            title = getString(R.string.action_toggle_favorite)
        }
    }

    override fun onResume() {
        super.onResume()

        lifecycleScope.launch(Dispatchers.IO) {
            libraryViewModel.apply {
                forceReload(ReloadType.Favorites)
                forceReload(ReloadType.FavoriteMedias)
                forceReload(ReloadType.Songs)
                forceReload(ReloadType.Playlists)
            }
        }
        setIsFavorite()
    }
}