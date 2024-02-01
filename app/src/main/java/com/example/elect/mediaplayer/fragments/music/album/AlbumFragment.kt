package com.example.elect.mediaplayer.fragments.music.album

import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.EXTRA_ALBUM_ID
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.album.AlbumAdapter
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseMusicRVFragment
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.IAlbumClickListener
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.example.elect.mediaplayer.views.TopAppBarLayout
import com.google.android.material.color.MaterialColors

class AlbumFragment
    : BaseMusicRVFragment<AlbumAdapter, GridLayoutManager>
    (R.layout.fragment_music_recycler_view),
    IAlbumClickListener,
    ICabHolder{

    private var cab: AttachedCab? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        libraryViewModel
            .getAlbums()
            .observe(viewLifecycleOwner){
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

    override fun createAdapter(): AlbumAdapter? {
        val dataSet =
            if(adapter == null) mutableListOf()
            else adapter!!.dataSet

        return AlbumAdapter(
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

    override fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        val currentGridSize : Int = getGridSize()

        gridSizeMenu.clear()

        gridSizeMenu.add(
            0,
            R.id.action_album_grid_size_1,
            0,
            getIntRes(R.integer.grid_1).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_1)

        gridSizeMenu.add(
            0,
            R.id.action_album_grid_size_2,
            1,
            getIntRes(R.integer.grid_2).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_2)

        gridSizeMenu.add(
            0,
            R.id.action_album_grid_size_3,
            2,
            getIntRes(R.integer.grid_3).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_3)

        gridSizeMenu.add(
            0,
            R.id.action_album_grid_size_4,
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
            R.id.action_album_sort_order_asc,

            0,

            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_desc,

            1,

            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_Z_A

        sortOrderMenu.add(
            0,
            R.id.action_album_sort_order_number_of_songs,

            2,

            R.string.sort_order_number_of_songs
        ).isChecked =
            currentSortOrder == SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS

        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean{
        val gridSize = when(item.itemId){
            R.id.action_album_grid_size_1 -> getIntRes(R.integer.grid_1)
            R.id.action_album_grid_size_2 -> getIntRes(R.integer.grid_2)
            R.id.action_album_grid_size_3 -> getIntRes(R.integer.grid_3)
            R.id.action_album_grid_size_4 -> getIntRes(R.integer.grid_4)
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
            R.id.action_album_sort_order_asc ->
                SortOrder.AlbumSortOrder.ALBUM_A_Z
            R.id.action_album_sort_order_desc ->
                SortOrder.AlbumSortOrder.ALBUM_Z_A
            R.id.action_album_sort_order_number_of_songs ->
                SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
            else -> PreferenceUtil.albumSortOrder
        }

        if(sortOrder != PreferenceUtil.albumSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.albumGridSize
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.albumSortOrder
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        PreferenceUtil.albumGridSize = gridSize

        findNavController().navigate(R.id.action_music)
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.albumSortOrder = sortOrder

        libraryViewModel.forceReload(ReloadType.Albums)
    }

    override fun onAlbumClick(albumId: Long, view: View) {

        findNavController().navigate(
            R.id.action_albumDetails,
            bundleOf(EXTRA_ALBUM_ID to albumId),
            null,

            FragmentNavigatorExtras(

                view to albumId.toString()
            )
        )

        reenterTransition = null
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