package com.example.elect.mediaplayer.fragments.player

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.adapter.player.CoverJacketAdapter
import com.example.elect.mediaplayer.databinding.FragmentPlayerCoverJacketBinding
import com.example.elect.mediaplayer.fragments.base.BaseMusicServiceFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.transform.CarousalPagerTransformer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlayerCoverJacketFragment:
    BaseMusicServiceFragment(R.layout.fragment_player_cover_jacket) {

    private var _binding: FragmentPlayerCoverJacketBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentPlayerCoverJacketBinding.bind(view)

        updatePlayingQueue()

        setViewPager2Register()

        setViewPager2()
    }

    private fun setViewPager2Register() {
        binding.viewPager2.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {

                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) { }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)


                    if (
                        position != PlayerRemote.position
                    ) {
                        PlayerRemote.playMediaAt(position)

                        if(
                            PlayerRemote.playingQueue[position].isSongOrVideo == 2
                        ) {
                            if(activity is PlayerActivity){
                                (activity as PlayerActivity).createPlayerFragment(
                                    PlayerRemote.playingQueue[position].isSongOrVideo
                                )
                            }
                        }
                    }
                }
            }
        )
    }

    private fun setViewPager2(){

        val metrics = resources.displayMetrics

        val ratio = metrics.heightPixels.toFloat() / metrics.widthPixels.toFloat()


        binding.viewPager2.clipToPadding = false

        val padding =
            if (ratio >= 1.777f) {
                40
            } else {
                100
            }

        binding.viewPager2.setPadding(
            padding,
            0,
            padding,
            0
        )

        binding.viewPager2.setPageTransformer(
            CarousalPagerTransformer(requireContext())
        )
    }


    private fun updatePlayingQueue() {

        binding.viewPager2.apply {

            adapter = CoverJacketAdapter(
                requireActivity(),
                PlayerRemote.playingQueue
            )

            adapter?.notifyDataSetChanged()

            currentItem = PlayerRemote.position

            setCurrentItem(PlayerRemote.position, false)

            if(currentItem != PlayerRemote.position){
                PlayerRemote.playMediaAt(PlayerRemote.position)
            }
        }
    }

    override fun onServiceConnected() {
        updatePlayingQueue()
    }

    override fun onPlayingMetaChanged() {
        binding.viewPager2.currentItem = PlayerRemote.position
        binding.viewPager2.setCurrentItem(PlayerRemote.position, false)
        if(PlayerRemote.currentMedia.isSongOrVideo == 2){
            if(activity is PlayerActivity){
                (activity as PlayerActivity).createPlayerFragment(
                    PlayerRemote.currentMedia.isSongOrVideo
                )
            }

            PlayerRemote.pauseMedia()
        }
    }

    override fun onQueueChanged() {
        updatePlayingQueue()
    }
}