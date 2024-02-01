package com.example.elect.mediaplayer.fragments.base

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.LayoutRes
import androidx.core.view.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.adapter.playlist.PlaylistSongAdapter
import com.example.elect.mediaplayer.databinding.FragmentAlbumArtistDetailsBinding
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.service.MusicService
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder

abstract class BaseAlbumArtistDetailsRVFragment
<A: RecyclerView.Adapter<*>>(
    @LayoutRes layout: Int
) : BaseToolbarFragment(layout){

    private var _binding: FragmentAlbumArtistDetailsBinding? = null
    val binding get() = _binding!!

    protected var adapterALAR: A? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        val transition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = android.graphics.Color.TRANSPARENT
            setAllContainerColors(android.graphics.Color.TRANSPARENT)
            setPathMotion(MaterialArcMotion())
        }

        sharedElementEnterTransition = transition

        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        _binding = FragmentAlbumArtistDetailsBinding.bind(view)

        super.onViewCreated(view, savedInstanceState)


        ViewCompat.setTransitionName(
            binding.coverContainer,
            setTransitionId()
        )

        setAppBarLayout()

        setUpRecyclerView()



        binding.appBarLayout.toolbar.apply {

            setNavigationOnClickListener {
                findNavController().navigateUp()
            }


            setNavigationIcon(
                R.drawable.ic_round_keyboard_backspace
            )
        }


        binding.fragmentRVLayout.backgroundColor(R.color.md_grey_900)
        setActionClickListener()
    }

    private fun setActionClickListener(){
        binding.fragmentContent.playAction.apply {
            setOnClickListener {
                val activity = requireActivity()
                if(activity is MainActivity){
                    activity.selectShowBottom()
                }
                PlayerRemote.openQueue(
                    returnMedias(),
                    0,
                    true
                )
            }

            textColor(R.color.md_blue_grey_200)
            backgroundColor(R.color.md_grey_900)
            strokeColor(R.color.md_blue_grey_200)
            iconColor(R.color.md_blue_500)
            rippleColor(R.color.md_blue_grey_200)
        }
        binding.fragmentContent.shuffleAction.apply {
            setOnClickListener {
                val activity = requireActivity()
                if(activity is MainActivity){
                    activity.selectShowBottom()
                }
                PlayerRemote.openAndShuffleQueue(
                    returnMedias(),
                    true
                )
            }

            textColor(R.color.md_black_1000)
            backgroundColor(R.color.md_blue_500)
            strokeColor(R.color.md_blue_500)
            iconColor(R.color.md_black_1000)
            rippleColor(R.color.md_blue_grey_200)
        }
    }

    abstract fun returnMedias(): List<Media>

    private fun setAppBarLayout(){
        val toolbar = binding.appBarLayout.toolbar

        mainActivity.setSupportActionBar(toolbar)

        toolbar.backgroundColor(R.color.md_blue_grey_900)
        binding.appBarLayout.setTextColor(R.color.md_white_1000)

        mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appBarLayout.setExpanded(true, true)
    }

    private fun setUpRecyclerView() {
        if(createAdapter() != null){
            adapterALAR = createAdapter()

            binding.fragmentContent.insetsConstraintLayout.apply {
                layoutAnimation = AnimationUtils
                    .loadLayoutAnimation(
                        requireContext(),
                        R.anim.item_appearance
                    )
            }

            binding.fragmentContent.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = adapterALAR

                createFastScroller(this)
            }

            adapterALAR?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()



                        checkForPadding()
                    }
                }
            )
        }

        checkForPadding()
    }


    protected open fun createFastScroller(
        recyclerView: RecyclerView
    ): FastScroller {
        val fastScroller = FastScrollerBuilder(recyclerView).useMd2Style()

        fastScroller.textAndBackgroundColor(
            requireContext(),
            R.color.md_blue_grey_200,
            R.color.md_blue_500
        )
        fastScroller.scrollerColor(
            requireContext(),
            R.color.md_blue_500
        )
        return fastScroller.build()
    }

    private fun checkForPadding() {
        binding.fragmentContent.recyclerView.updatePadding(
            bottom = if(
                PlayerRemote.isPlaying ||
                PlayerRemote.playingQueue.isNotEmpty()
            ) {
                dip(R.dimen.mini_player_height_expanded)
            } else{
                dip(R.dimen.bottom_nav_height)
            }
        )
    }

    abstract fun setTransitionId(): String
    protected abstract fun createAdapter(): A?

    fun setToolbarTitle(string: String){
        binding.appBarLayout.title = string
        binding.appBarLayout.toolbar.setTitleTextAppearance(
            context, R.style.ToolbarTextAppearanceNormal
        )
    }

    fun favoriteClicked(
        media: Media,
        isFavorite: Boolean,
        image: ImageView?
    ){

        lifecycleScope.launch(Dispatchers.IO){

            libraryViewModel.insertOrUpdateMediaFavoriteEntity(media, isFavorite)

            libraryViewModel.forceReload(ReloadType.FavoriteMedias)

            withContext(Dispatchers.Main){
                val icon = if(isFavorite){
                    GlideExtensions.DEFAULT_FAVORITE_TRUE
                }else {
                    GlideExtensions.DEFAULT_FAVORITE_FALSE
                }

                image?.apply {
                    setImageResource(icon)

                    if(isFavorite){
                        iconColor(R.color.md_blue_grey_400)
                    } else {
                        iconColor(R.color.md_white_1000)
                    }
                }

                requireContext().sendBroadcast(
                    Intent(MusicService.FAVORITE_STATE_CHANGED)
                )
            }
        }
    }


    override fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        return
    }

    override fun onSetSelectedItem(
        item: MenuItem
    ): Boolean {
        if(handleSortOrderMenuItem(item)){
            return true
        }

        if(selectedAddPlaylist(item)){
            return true
        }

        return false
    }

    abstract fun selectedAddPlaylist(item: MenuItem): Boolean

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        return true
    }

    override fun loadGridSize(): Int {
        return 0
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        return
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}