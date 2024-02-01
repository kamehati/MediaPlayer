package com.example.elect.mediaplayer.fragments.music.song

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseMusicRVFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.example.elect.mediaplayer.views.TopAppBarLayout
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class SongFragment
    : BaseMusicRVFragment<SongAdapter, GridLayoutManager>
    (R.layout.fragment_music_recycler_view),
    ICabHolder{

    private var cab: AttachedCab? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        libraryViewModel.apply {
            getMediaSongs()
                .observe(viewLifecycleOwner){
                    if(it.isNotEmpty())
                        adapter?.swapDataSet(it)
                    else
                        adapter?.swapDataSet(listOf())
                }

            getMediaFavoriteEntities()
                .observe(viewLifecycleOwner){
                    if (adapter is SongAdapter){
                        if(it.isNotEmpty())
                            adapter?.syncMediaFavoriteEntity(it)
                        else
                            adapter?.syncMediaFavoriteEntity(listOf())
                    }
                }
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


    override val isShuffleVisible: Boolean
        get() = true

    override fun createAdapter(): SongAdapter? {
        val dataSet =
            if(adapter == null) mutableListOf()
            else adapter!!.dataSet


        return SongAdapter(
            requireActivity(),
            dataSet,
            itemLayoutRes(),
            this,
            this,
            R.menu.cab_holder_menu
        )
    }


    override fun createLayoutManager(): GridLayoutManager? {
        return GridLayoutManager(
            requireActivity(),
            getGridSize()
        )
    }

    override fun onShuffleClicked() {
        Toast.makeText(
            requireContext(),
            "Click ShuffleBtn",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        val currentGridSize : Int = getGridSize()

        gridSizeMenu.clear()

        gridSizeMenu.add(
            0,
            R.id.action_song_grid_size_1,
            0,
            getIntRes(R.integer.grid_1).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_1)

        gridSizeMenu.add(
            0,
            R.id.action_song_grid_size_2,
            1,
            getIntRes(R.integer.grid_2).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_2)

        gridSizeMenu.add(
            0,
            R.id.action_song_grid_size_3,
            2,
            getIntRes(R.integer.grid_3).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_3)

        gridSizeMenu.add(
            0,
            R.id.action_song_grid_size_4,
            3,
            getIntRes(R.integer.grid_4).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_4)

        gridSizeMenu.setGroupCheckable(0,true, true)
    }

    override fun setUpSortOrderMenu(
        sortOrderMenu: SubMenu
    ) {
        val currentSortOrder: String? = getSortOrder()
        sortOrderMenu.clear()


        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_asc,

            0,

            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_desc,

            1,

            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_Z_A

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_artist,

            2,

            R.string.sort_order_artist
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_ARTIST

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_album,

            3,

            R.string.sort_order_album
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_ALBUM

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_year,

            4,

            R.string.sort_order_year
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_YEAR

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_date,

            5,

            R.string.sort_order_date
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_DATE

        sortOrderMenu.add(
            0,
            R.id.action_song_sort_order_date_modified,

            5,

            R.string.sort_order_date_modified
        ).isChecked =
            currentSortOrder == SortOrder.SongSortOrder.SONG_DATE_MODIFIED

        sortOrderMenu.setGroupCheckable(0,true, true)
    }


    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean{
        val gridSize = when(item.itemId){
            R.id.action_song_grid_size_1 -> getIntRes(R.integer.grid_1)
            R.id.action_song_grid_size_2 -> getIntRes(R.integer.grid_2)
            R.id.action_song_grid_size_3 -> getIntRes(R.integer.grid_3)
            R.id.action_song_grid_size_4 -> getIntRes(R.integer.grid_4)
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
            R.id.action_song_sort_order_asc ->
                SortOrder.SongSortOrder.SONG_A_Z
            R.id.action_song_sort_order_desc ->
                SortOrder.SongSortOrder.SONG_Z_A
            R.id.action_song_sort_order_artist ->
                SortOrder.SongSortOrder.SONG_ARTIST
            R.id.action_song_sort_order_album ->
                SortOrder.SongSortOrder.SONG_ALBUM
            R.id.action_song_sort_order_year ->
                SortOrder.SongSortOrder.SONG_YEAR
            R.id.action_song_sort_order_date ->
                SortOrder.SongSortOrder.SONG_DATE
            R.id.action_song_sort_order_date_modified ->
                SortOrder.SongSortOrder.SONG_DATE_MODIFIED
            else -> PreferenceUtil.songSortOrder
        }

        if(sortOrder != PreferenceUtil.songSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.songGridSize
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.songSortOrder
    }


    override fun setAndSaveGridSize(gridSize: Int) {
        PreferenceUtil.songGridSize = gridSize


        findNavController().navigate(R.id.action_music)

    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.songSortOrder = sortOrder

        libraryViewModel.forceReload(ReloadType.Songs)
    }

    fun bottomNavSelect(appBarLayout: TopAppBarLayout) {
        if(recyclerView == null){

            appBarLayout.setExpanded(true, true)
        } else {
            recyclerView!!.scrollToPosition(0)

            appBarLayout.setExpanded(true, true)
        }
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
}