package com.example.elect.mediaplayer.fragments.playlist

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.EXTRA_PLAYLIST
import com.example.elect.mediaplayer.EXTRA_PLAYLIST_ID
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.playlist.PlaylistAdapter
import com.example.elect.mediaplayer.db.PlaylistWithMedias
import com.example.elect.mediaplayer.dialogs.CreatePlaylistDialog
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseRVFragment
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.interfaces.IPlaylistClickListener
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class PlaylistFragment
    : BaseRVFragment<PlaylistAdapter, GridLayoutManager>
    (R.layout.fragment_recycler_view),
    IPlaylistClickListener,
    ICabHolder{

    private var cab: AttachedCab? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(resources.getString(R.string.playlist))

        libraryViewModel
            .getMediaPlaylists()
            .observe(viewLifecycleOwner) {
                if(it.isNotEmpty())
                    adapter?.swapDataSet(it)
                else
                    adapter?.swapDataSet(listOf())
            }


        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {

                if (!handleBackPress()) {

                    remove()
                    requireActivity().finish()
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

    override val isShuffleVisible: Boolean
        get() = false

    override fun createAdapter(): PlaylistAdapter? {

        val dataSet =
            if (adapter == null) mutableListOf()
            else adapter!!.dataSet

        return PlaylistAdapter(
            requireActivity(),
            dataSet,
            itemLayoutRes(),
            this,
            this
        )
    }

    override fun createLayoutManager(): GridLayoutManager? {
        return GridLayoutManager(
            requireActivity(),
            getGridSize()
        )
    }

    override fun onShuffleClicked() {
        super.onShuffleClicked()
    }

    override fun onSetSelectedItem(
        item: MenuItem
    ): Boolean {
        if(
            item.itemId ==
            R.id.action_add_new_playlist
        ){
            CreatePlaylistDialog
                .create(mutableListOf(), mainActivity)
                .show(
                    mainActivity.
                    supportFragmentManager,
                    "Dialog"
                )

            return true
        }

        return super.onSetSelectedItem(item)
    }

    override fun setUpGridSizeMenu(gridSizeMenu: SubMenu) {
        val currentGridSize : Int = getGridSize()

        gridSizeMenu.clear()

        gridSizeMenu.add(
            0,
            R.id.action_playlist_grid_size_1,
            0,
            getIntRes(R.integer.grid_1).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_1)

        gridSizeMenu.add(
            0,
            R.id.action_playlist_grid_size_2,
            1,
            getIntRes(R.integer.grid_2).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_2)

        gridSizeMenu.add(
            0,
            R.id.action_playlist_grid_size_3,
            2,
            getIntRes(R.integer.grid_3).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_3)

        gridSizeMenu.setGroupCheckable(0,true, true)
    }

    override fun setUpSortOrderMenu(
        sortOrderMenu: SubMenu
    ) {
        val currentSortOrder: String? = getSortOrder()
        sortOrderMenu.clear()

        sortOrderMenu.add(
            0,
            R.id.action_playlist_sort_order_asc,
            0,
            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.PlaylistSortOrder.PLAYLIST_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_playlist_sort_order_desc,
            1,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.PlaylistSortOrder.PLAYLIST_Z_A

        sortOrderMenu.add(
            0,
            R.id.action_playlist_sort_order_song_count_asc,
            2,
            R.string.sort_order_song_count_asc
        ).isChecked =
            currentSortOrder == SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_ASC

        sortOrderMenu.add(
            0,
            R.id.action_playlist_sort_order_song_count_desc,
            3,
            R.string.sort_order_song_count_desc
        ).isChecked =
            currentSortOrder == SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_DESC

        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        val gridSize = when(item.itemId){
            R.id.action_playlist_grid_size_1 -> getIntRes(R.integer.grid_1)
            R.id.action_playlist_grid_size_2 -> getIntRes(R.integer.grid_2)
            R.id.action_playlist_grid_size_3 -> getIntRes(R.integer.grid_3)
            else -> 0
        }

        if(gridSize > 0){
            item.isChecked = true
            setAndSaveGridSize(gridSize)

            return true
        }

        return false
    }

    override fun handleSortOrderMenuItem(
        item: MenuItem
    ): Boolean {
        val sortOrder: String = when(item.itemId){
            R.id.action_playlist_sort_order_asc ->
                SortOrder.PlaylistSortOrder.PLAYLIST_A_Z
            R.id.action_playlist_sort_order_desc ->
                SortOrder.PlaylistSortOrder.PLAYLIST_Z_A
            R.id.action_playlist_sort_order_song_count_asc ->
                SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_ASC
            R.id.action_playlist_sort_order_song_count_desc ->
                SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_DESC
            else -> PreferenceUtil.playlistSortOrder
        }

        if(sortOrder != PreferenceUtil.playlistSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.playlistGridSize
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.playlistSortOrder
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        PreferenceUtil.playlistGridSize = gridSize

        invalidateLayoutManager()
        invalidateAdapter()

        recyclerView?.startLayoutAnimation()

        scrollToTop()
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.playlistSortOrder = sortOrder

        libraryViewModel.forceReload(ReloadType.Playlists)
    }

    override fun onPlaylistClick(playlist: PlaylistWithMedias) {
        findNavController().navigate(
            R.id.action_playlistDetails,
            bundleOf(EXTRA_PLAYLIST to playlist),
            null,
            null
        )
        reenterTransition = null
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

    override fun onPause() {
        super.onPause()
        if (cab.isActive()) {
            cab.destroy()
        }
    }

    override fun onResume() {
        super.onResume()

        libraryViewModel
            .forceReload(ReloadType.Playlists)
    }
}