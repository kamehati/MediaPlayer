package com.example.elect.mediaplayer.activity

import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.NavController
import androidx.navigation.navOptions
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.base.BaseMusicServiceActivity
import com.example.elect.mediaplayer.databinding.ActivityPlayerBinding
import com.example.elect.mediaplayer.extensions.currentFragment
import com.example.elect.mediaplayer.extensions.findNavController
import com.example.elect.mediaplayer.extensions.whichFragment
import com.example.elect.mediaplayer.fragments.base.BasePlayerFragment
import com.example.elect.mediaplayer.fragments.other.MiniPlayerFragment
import com.example.elect.mediaplayer.fragments.player.MusicPlayerFragment
import com.example.elect.mediaplayer.fragments.player.VideoPlayerFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.interfaces.IBottomNavSelectedHelper
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.BuildUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior

class PlayerActivity: BaseMusicServiceActivity() {

    private lateinit var binding : ActivityPlayerBinding

    private var musicPlayerFragment: MusicPlayerFragment? = null
    private var videoPlayerFragment: VideoPlayerFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(MainActivity.IS_FADE_ACTIVITY){

            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }

        setUpNavigationController(
            PlayerRemote.currentMedia.isSongOrVideo
        )
    }

    private fun setUpNavigationController(
        isMedia: Int
    ){
        val navController =
            findNavController(R.id.playerFragmentContainer)

        with(navController){
            val navInflater = this.navInflater
            val navGraph = navInflater
                .inflate(R.navigation.player_graph)

            val playerId = when(
                isMedia
            ) {
                1 -> {
                    R.id.action_music_player
                }
                2 -> {
                    R.id.action_video_player
                }
                else -> {
                    R.id.action_music_player
                }
            }

            navGraph.setStartDestination(
                playerId
            )

            graph = navGraph


        }
    }


    fun createPlayerFragment(
        isMedia: Int
    ) {

        when(isMedia) {
            1 -> {
                val options = navOptions {
                    popUpTo(R.id.action_video_player){
                        inclusive = true
                    }
                }
                if(
                    currentFragment(R.id.playerFragmentContainer) is
                            VideoPlayerFragment
                ) {
                    findNavController(R.id.playerFragmentContainer)
                        .navigate(R.id.action_music_player)
                }
            }

            2 -> {
                val options = navOptions {
                    popUpTo(R.id.action_music_player){
                        inclusive = true
                    }
                }
                if(
                    currentFragment(R.id.playerFragmentContainer) is
                            MusicPlayerFragment
                ) {
                    findNavController(R.id.playerFragmentContainer)
                        .navigate(R.id.action_video_player)
                }
            }
        }
    }

    private fun setSystemBar(){
        if(BuildUtil.isRPlus()){

            WindowInsetsControllerCompat(
                window,
                window.decorView
            ).apply {
                show(WindowInsetsCompat.Type.systemBars())
            }

            if (BuildUtil.isPPlus()) {

                window.attributes.layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams
                        .LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }

            ViewCompat.setOnApplyWindowInsetsListener(
                window.decorView
            ) {
                    _, insets ->

                if (insets.displayCutout != null) {
                    insets
                } else {

                    WindowInsetsCompat.CONSUMED
                }
            }
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        finish()
    }

    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.playerFragmentContainer).navigateUp()


    override fun onResume() {
        super.onResume()

        if(
            currentFragment(R.id.playerFragmentContainer) is
                    MusicPlayerFragment
        ) {
            musicPlayerFragment = currentFragment(R.id.playerFragmentContainer) as MusicPlayerFragment
        } else if(
            currentFragment(R.id.playerFragmentContainer) is
                    VideoPlayerFragment
        ) {
            videoPlayerFragment = currentFragment(R.id.playerFragmentContainer) as VideoPlayerFragment
            PlayerRemote.pauseMedia()
        }
    }

    override fun onPause() {
        super.onPause()
        if (MainActivity.IS_FADE_ACTIVITY) {
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

            if(
                currentFragment(R.id.playerFragmentContainer) is
                        MusicPlayerFragment
            ) {
                musicPlayerFragment = currentFragment(R.id.playerFragmentContainer) as MusicPlayerFragment
                musicPlayerFragment?.onHide()
            } else if(
                currentFragment(R.id.playerFragmentContainer) is
                        VideoPlayerFragment
            ) {
                videoPlayerFragment = currentFragment(R.id.playerFragmentContainer) as VideoPlayerFragment
                videoPlayerFragment?.putVideoProgress()
                PlayerRemote.seekTo(PreferenceUtil.videoProgressMillis)
            }
        }
    }

    override fun onStop() {
        super.onStop()


        if(PlayerRemote.currentMedia.isSongOrVideo == 2){
            PlayerRemote.resumeMedia()
        }
    }

    override fun onDestroy() {
        super.onDestroy()


    }
}