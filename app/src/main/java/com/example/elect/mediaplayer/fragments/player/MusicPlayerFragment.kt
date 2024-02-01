package com.example.elect.mediaplayer.fragments.player

import android.media.audiofx.LoudnessEnhancer
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.Toolbar
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.databinding.FragmentMusicPlayerBinding
import com.example.elect.mediaplayer.dialogs.BrightnessDialog
import com.example.elect.mediaplayer.dialogs.MediaLoudnessDialog
import com.example.elect.mediaplayer.dialogs.QueueDialogFragment
import com.example.elect.mediaplayer.extensions.backgroundColor
import com.example.elect.mediaplayer.extensions.navigationBarColor
import com.example.elect.mediaplayer.extensions.statusBarColor
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseMusicServiceFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.PreferenceUtil
import kotlinx.coroutines.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class MusicPlayerFragment:
    BaseMusicServiceFragment(R.layout.fragment_music_player),
    Toolbar.OnMenuItemClickListener {

    private var _binding: FragmentMusicPlayerBinding? = null
    private val binding get() = _binding!!

    private lateinit var loudnessEnhancer: LoudnessEnhancer

    val libraryViewModel: LibraryViewModel by sharedViewModel()

    private var controlsFragment: PlayerActionControlsFragment? = null

    fun onShow() {
        controlsFragment?.buttonShow()

        CoroutineScope(Dispatchers.IO)
            .launch {
                delay(500)
                withContext(Dispatchers.Main){
                    controlsFragment?.show()
                }
            }
    }

    fun onHide() {
        controlsFragment?.hide()
        onBackPressed()
    }

    fun onBackPressed(): Boolean {
        return false
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentMusicPlayerBinding.bind(view)

        setColorGradient()

        setUpSubFragments()

        setUpPlayerToolbar()

        setStatusBar()
    }

    private fun setStatusBar() {
        requireActivity().apply {
            statusBarColor(R.color.md_grey_900)
            navigationBarColor(R.color.md_grey_900)
        }

        WindowCompat.setDecorFitsSystemWindows(
            requireActivity().window,
            true
        )

        WindowInsetsControllerCompat(
            requireActivity().window,
            binding.root
        ).let { controller ->
            controller.show(
                WindowInsetsCompat.Type.systemBars()
            )
        }
    }

    private fun setColorGradient() {
        binding.colorGradientBackground.apply {
            visibility = View.VISIBLE
            backgroundColor(R.color.md_grey_900)
        }
    }

    private fun setUpSubFragments() {
        controlsFragment = childFragmentManager
            .findFragmentById(
                R.id.actionControlsFragment
            ) as PlayerActionControlsFragment
    }

    private fun setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_music_player)
        binding.playerToolbar.setNavigationOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.playerToolbar.setOnMenuItemClickListener(this)

        binding.playerToolbar.backgroundColor(R.color.md_grey_900)

        setUpIsFavorite()
    }

    private fun setUpIsFavorite() {
        val media = PlayerRemote.currentMedia
        CoroutineScope(Dispatchers.IO).launch {

            val isFavorite = libraryViewModel.isMediaFavoriteEntity(media.id)

            withContext(Dispatchers.Main){
                val icon = if(isFavorite){
                    GlideExtensions.DEFAULT_FAVORITE_TRUE
                }else {
                    GlideExtensions.DEFAULT_FAVORITE_FALSE
                }

                setImageResource(icon)
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem): Boolean {
        val media = PlayerRemote.currentMedia

        return when(item.itemId) {
            R.id.action_toggle_favorite -> {
                toggleFavorite(media)
                true
            }

            R.id.action_brightness -> {
                val activity = requireActivity() as PlayerActivity
                BrightnessDialog.newInstance(activity).show(
                    activity.supportFragmentManager,
                    "BRIGHTNESS_DIALOG"
                )
                true
            }

            R.id.action_loudness -> {
                val activity = requireActivity() as PlayerActivity
                MediaLoudnessDialog.newInstance(activity).show(
                    activity.supportFragmentManager,
                    "MEDIA_LOUDNESS_DIALOG"
                )
                true
            }

            R.id.action_queue -> {
                val dialog = QueueDialogFragment.newInstance()
                activity?.supportFragmentManager?.let {
                    dialog.show(it, dialog.tag)
                }
                true
            }

            else -> true
        }
    }

    private fun toggleFavorite(media: Media) {

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
        }
    }

    fun setImageResource(icon: Int) {
        binding.playerToolbar.menu?.findItem(
            R.id.action_toggle_favorite
        )?.apply {
            setIcon(icon)
            title = getString(R.string.action_toggle_favorite)
        }
    }

    override fun onResume() {
        super.onResume()

        onShow()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                remove()
                requireActivity().finish()
            }
    }

    private fun setLoudness() {
        setUpLoudness()
        val loudness = PreferenceUtil.mediaLoudNess
        if(loudness < 0 || loudness > 100){
            PreferenceUtil.mediaLoudNess = 50
        }
        loudnessEnhancer.setTargetGain(
            (loudness - 50) * 50
        )
    }

    private fun setUpLoudness(){
        loudnessEnhancer = LoudnessEnhancer(PlayerRemote.audioSessionId)
        loudnessEnhancer.enabled = true
    }

    override fun onPlayingMetaChanged() {
        CoroutineScope(Dispatchers.IO).launch{
            delay(1000)
            setLoudness()
        }
    }

    override fun onPlayStateChanged() {
    }

    override fun onPause() {
        super.onPause()

        onHide()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): MusicPlayerFragment {
            return MusicPlayerFragment()
        }
    }
}