package com.example.elect.mediaplayer.fragments.playlist

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.view.animation.*
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.annotation.StringRes
import androidx.core.view.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.adapter.playlist.PlaylistSongAdapter
import com.example.elect.mediaplayer.databinding.FragmentPlaylistDetailsBinding
import com.example.elect.mediaplayer.db.PlaylistWithMedias
import com.example.elect.mediaplayer.db.PlaylistWithSongs
import com.example.elect.mediaplayer.db.toMedias
import com.example.elect.mediaplayer.db.toSongs
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.dialogs.DeletePlaylistDialog
import com.example.elect.mediaplayer.dialogs.RenamePlaylistDialog
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseToolbarFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import com.h6ah4i.android.widget.advrecyclerview.animator.DraggableItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlaylistDetailsFragment
    : BaseToolbarFragment(
    R.layout.fragment_playlist_details
), ICabHolder {

    private var cab: AttachedCab? = null

    private var _binding: FragmentPlaylistDetailsBinding? = null
    private val binding get() = _binding!!

    private val arguments by navArgs<PlaylistDetailsFragmentArgs>()


    private val viewModel by viewModel<PlaylistDetailsViewModel> {

        parametersOf(arguments.extraPlaylist)
    }

    private lateinit var playlistSongAdapter: PlaylistSongAdapter
    private lateinit var playlist: PlaylistWithMedias

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        _binding = FragmentPlaylistDetailsBinding.bind(view)

        super.onViewCreated(view, savedInstanceState)


        val menuHost: MenuHost = requireActivity()
        onSetOptionsMenu(menuHost)

        setAppBarLayout()

        setToolbarTitle(R.string.playlist)

        playlist = arguments.extraPlaylist

        setText(
            playlist.playlistEntity.playlistName,
            resources.getString(R.string.song)
        )


        viewModel.apply {
            getMedias()
            .observe(viewLifecycleOwner) {
                medias(it.toMedias())
            }

            playlistExists()
                .observe(viewLifecycleOwner){
                    if(!it){
                        libraryViewModel.forceReload(ReloadType.Playlists)

                        findNavController().navigateUp()
                    }
                }
        }

        setUpRecyclerView()

        libraryViewModel
            .getMediaFavoriteEntities()
            .observe(viewLifecycleOwner){
                if(it.isNotEmpty())
                    playlistSongAdapter.syncMediaFavoriteEntity(it)
                else
                    playlistSongAdapter.syncMediaFavoriteEntity(listOf())
            }


        postponeEnterTransition()


        view.doOnPreDraw {

            startPostponedEnterTransition()
        }

        binding.appBarLayout.toolbar.apply {

            setNavigationOnClickListener {
                PreferenceUtil.appbarMode = 0
                findNavController().navigateUp()
            }


            setNavigationIcon(
                R.drawable.ic_round_keyboard_backspace
            )
        }


        binding.fragmentRVLayout.backgroundColor(R.color.md_grey_900)

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                if (!handleBackPress()) {
                    remove()
                    PreferenceUtil.appbarMode = 0
                    findNavController().navigateUp()
                }
            }
    }


    private fun handleBackPress(): Boolean {
        cab?.let {

            if (it.isActive()) {

                it.destroy()
                return true
            }
        }
        return false
    }

    private fun onSetOptionsMenu(menuHost: MenuHost){
        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {

                }

                override fun onMenuItemSelected(
                    menuItem: MenuItem
                ): Boolean {
                    when(menuItem.itemId){
                        R.id.action_settings ->
                            Toast.makeText(
                                requireContext(),
                                "Settings",
                                Toast.LENGTH_SHORT
                            ).show()
                    }

                    return onSetSelectedItem(menuItem)
                }
            },
            viewLifecycleOwner,

            Lifecycle.State.RESUMED
        )
    }

    private fun medias(medias: List<Media>) {
        if (medias.isNotEmpty()) {
            playlistSongAdapter.swapDataSet(medias)
        } else {
            showEmptyView()
        }
    }

    private fun setUpRecyclerView() {


        val dragDropManager = RecyclerViewDragDropManager()

        playlistSongAdapter = PlaylistSongAdapter(
            mainActivity,
            ArrayList(),
            R.layout.item_list,
            this,
            this,
            playlist.playlistEntity
        )


        val wrappedAdapter: RecyclerView.Adapter<*> =
            dragDropManager.createWrappedAdapter(playlistSongAdapter)


        val animator: GeneralItemAnimator = DraggableItemAnimator()

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = wrappedAdapter
            itemAnimator = animator
            layoutAnimation = AnimationUtils
                .loadLayoutAnimation(
                    requireContext(),
                    R.anim.item_appearance
                )

            createFastScroller(this)


            dragDropManager.attachRecyclerView(this)
        }

        playlistSongAdapter.registerAdapterDataObserver(
            object : RecyclerView.AdapterDataObserver() {
                override fun onChanged() {
                    super.onChanged()

                    checkIsEmpty()

                    checkForPadding()
                }
            }
        )

        checkForPadding()
    }

    private fun checkForPadding() {
        binding.recyclerView.updatePadding(
            bottom = if(
                PlayerRemote.isPlaying ||
                PlayerRemote.playingQueue.isNotEmpty()
            ) {
                dip(R.dimen.mini_player_height)
            } else{
                dip(R.dimen.status_bar_padding)
            }
        )
    }

    private val emptyMessage: Int
        @StringRes get() = R.string.empty

    private fun checkIsEmpty(){
        binding.emptyText.setText(emptyMessage)
        binding.empty.isVisible =
            playlistSongAdapter.itemCount == 0
        binding.emptyText.isVisible =
            playlistSongAdapter.itemCount == 0
    }

    private fun showEmptyView() {
        binding.empty.isVisible = true
        binding.emptyText.isVisible = true
    }

    private fun setAppBarLayout(){
        val toolbar = binding.appBarLayout.toolbar

        mainActivity.setSupportActionBar(toolbar)

        toolbar.backgroundColor(R.color.md_blue_grey_900)
        binding.appBarLayout.setTextColor(R.color.md_white_1000)

        mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appBarLayout.setExpanded(true, true)
    }

    fun setToolbarTitle(@StringRes stringId: Int){

        binding.appBarLayout.title = resources.getString(stringId)
        binding.appBarLayout.toolbar.setTitleTextAppearance(
            context, R.style.ToolbarTextAppearanceNormal
        )
    }

    private fun setText(st1: String, st2: String){

    }



    override fun setUpSortOrderMenu(
        sortOrderMenu: SubMenu
    ) {
        return
    }

    override fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        return
    }

    override fun onSetSelectedItem(
        item: MenuItem
    ): Boolean {

        when (item.itemId) {
            R.id.action_add_to_playlist ->
                CoroutineScope(Dispatchers.IO)
                    .launch {
                        val playlists = get<RealRepository>()
                            .fetchPlaylists()
                        withContext(Dispatchers.Main) {
                            AddToPlaylistDialog
                                .create(
                                    playlists,
                                    playlistSongAdapter.dataSet,
                                    mainActivity
                                ).show(
                                    mainActivity.supportFragmentManager,
                                    "ADD_PLAYLIST"
                                )
                        }
                    }


            R.id.action_rename_playlist -> {
                RenamePlaylistDialog
                    .create(playlist.playlistEntity)
                    .show(
                        mainActivity.supportFragmentManager,
                        "RENAME_PLAYLIST"
                    )

                return true
            }


            R.id.action_delete_playlist -> {
                DeletePlaylistDialog
                    .create(playlist.playlistEntity, navController = findNavController())
                    .show(
                        mainActivity.supportFragmentManager,
                        "DELETE_PLAYLIST"
                    )

                return true
            }
        }

        return false
    }

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        return true
    }

    override fun handleSortOrderMenuItem(
        item: MenuItem
    ): Boolean {
        return true
    }

    override fun loadGridSize(): Int {
        return 0
    }

    override fun loadSortOrder(): String? {
        return ""
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        return
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        return
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

    override fun onStart() {
        super.onStart()

        binding.recyclerView.startLayoutAnimation()
    }


    override fun onResume() {
        super.onResume()

        binding.recyclerView.startLayoutAnimation()
    }

    override fun onPause() {
        if (cab.isActive()) {
            cab.destroy()
        }


        playlistSongAdapter.saveSongs(
            playlist.playlistEntity
        )

        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun openCab(
        menuRes: Int,
        callback: ICabCallback
    ): AttachedCab {
        cab?.let {

            if (it.isActive()) {
                it.destroy()
            }
        }


        cab = createCab(R.id.toolbar_container) {

            menu(menuRes)

            closeDrawable(R.drawable.ic_round_close)

            backgroundColor(
                literal = MaterialColors.getColor(
                    requireView(),
                    R.attr.cabHolderColor
                )
            )

            slideDown()
            onCreate { cab, menu ->

                callback.onCabCreated(cab, menu)
            }
            onSelection {

                callback.onCabItemClicked(it)
            }
            onDestroy {

                callback.onCabFinished(it)
            }
        }

        return cab as AttachedCab
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
}