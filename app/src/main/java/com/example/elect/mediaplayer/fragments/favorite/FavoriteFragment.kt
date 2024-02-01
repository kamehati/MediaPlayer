package com.example.elect.mediaplayer.fragments.favorite

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.favorite.FavoriteAdapter
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseRVFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFade
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get

class FavoriteFragment
    : BaseRVFragment<FavoriteAdapter, GridLayoutManager>
    (R.layout.fragment_recycler_view),
    ICabHolder {

    private var cab: AttachedCab? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(resources.getString(R.string.favorite))


        libraryViewModel
            .getFavoriteMedias()
            .observe(viewLifecycleOwner){
            if (it.isNotEmpty())
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
        get() = false

    override fun createAdapter(): FavoriteAdapter? {
        val dataSet =
            if(adapter == null) mutableListOf()
            else adapter!!.dataSet

        return FavoriteAdapter(
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
            R.id.action_add_to_playlist
        ){
            CoroutineScope(Dispatchers.IO)
                .launch {
                    val playlists = get<RealRepository>()
                        .fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        if (adapter != null) {
                            AddToPlaylistDialog
                                .create(playlists, adapter!!.dataSet, mainActivity)
                                .show(
                                    mainActivity.supportFragmentManager,
                                    "ADD_PLAYLIST"
                                )
                        }
                    }
                }

            return true
        }

        return super.onSetSelectedItem(item)
    }

    override fun setUpGridSizeMenu(gridSizeMenu: SubMenu) {
        val currentGridSize : Int = getGridSize()

        gridSizeMenu.clear()

        gridSizeMenu.add(
            0,
            R.id.action_favorite_grid_size_1,
            0,
            getIntRes(R.integer.grid_1).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_1)

        gridSizeMenu.add(
            0,
            R.id.action_favorite_grid_size_2,
            1,
            getIntRes(R.integer.grid_2).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_2)

        gridSizeMenu.setGroupCheckable(0,true, true)
    }

    override fun setUpSortOrderMenu(
        sortOrderMenu: SubMenu
    ) {
        val currentSortOrder: String? = getSortOrder()
        sortOrderMenu.clear()

        sortOrderMenu.add(
            0,
            R.id.action_favorite_sort_order_asc,
            0,
            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.FavoriteSortOrder.FAVORITE_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_favorite_sort_order_desc,
            1,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.FavoriteSortOrder.FAVORITE_Z_A

        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        val gridSize = when(item.itemId){
            R.id.action_favorite_grid_size_1 -> getIntRes(R.integer.grid_1)
            R.id.action_favorite_grid_size_2 -> getIntRes(R.integer.grid_2)
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
            R.id.action_favorite_sort_order_asc ->
                SortOrder.FavoriteSortOrder.FAVORITE_A_Z
            R.id.action_favorite_sort_order_desc ->
                SortOrder.FavoriteSortOrder.FAVORITE_Z_A
            else -> PreferenceUtil.favoriteSortOrder
        }

        if(sortOrder != PreferenceUtil.favoriteSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.favoriteGridSize
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.favoriteSortOrder
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        PreferenceUtil.favoriteGridSize = gridSize

        invalidateLayoutManager()
        invalidateAdapter()

        recyclerView?.startLayoutAnimation()

        scrollToTop()
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.favoriteSortOrder = sortOrder

        libraryViewModel.forceReload(ReloadType.Favorites)
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