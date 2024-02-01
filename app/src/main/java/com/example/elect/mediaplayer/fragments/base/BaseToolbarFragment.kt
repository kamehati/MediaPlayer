package com.example.elect.mediaplayer.fragments.base

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.fragments.favorite.FavoriteFragment
import com.example.elect.mediaplayer.fragments.playlist.PlaylistDetailsFragment
import com.example.elect.mediaplayer.fragments.playlist.PlaylistFragment

abstract class BaseToolbarFragment(
    @LayoutRes layout: Int
) : BaseActivityFragment(layout) {

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setOptionsMenu()
    }

    fun setOptionsMenu() {
        val menuHost: MenuHost = requireActivity()
        onSetOptionsMenu(menuHost)
    }

    fun getGridSize(): Int{
        return  loadGridSize()
    }


    private fun onSetOptionsMenu(menuHost: MenuHost){
        menuHost.addMenuProvider(
            object : MenuProvider {

                override fun onCreateMenu(
                    menu: Menu,
                    menuInflater: MenuInflater
                ) {

                    menu.clear()
                    if(
                        this@BaseToolbarFragment is BaseRVFragment<*, *> ||
                        this@BaseToolbarFragment is BaseMusicRVFragment<*, *>
                    ){
                        menuInflater.inflate(R.menu.menu_main, menu)
                        if(
                            this@BaseToolbarFragment is PlaylistFragment
                        ){
                            menu.add(
                                0,
                                R.id.action_add_new_playlist,
                                0,
                                R.string.action_new_playlist
                            )
                        } else if (
                            this@BaseToolbarFragment is FavoriteFragment
                        ){
                            menu.add(
                                0,
                                R.id.action_add_to_playlist,
                                0,
                                R.string.action_add_to_playlist
                            )
                        }
                    } else if(
                        this@BaseToolbarFragment is BaseAlbumArtistDetailsRVFragment<*>
                    ) {
                        menuInflater.inflate(R.menu.menu_album_artist_details, menu)
                    } else if (
                        this@BaseToolbarFragment is BaseFolderDetailsRVFragment<*>
                    ){
                        menuInflater.inflate(R.menu.menu_folder_details, menu)
                    } else if(
                        this@BaseToolbarFragment is PlaylistDetailsFragment
                    ){
                        menuInflater.inflate(R.menu.menu_playlist_details, menu)
                    }

                    if(
                        menu.findItem(R.id.action_grid_size) != null &&
                        menu.findItem(R.id.action_grid_size).subMenu != null
                    ){
                        setUpGridSizeMenu(
                            menu.findItem(R.id.action_grid_size).subMenu!!
                        )
                    }
                    if(
                        menu.findItem(R.id.action_sort_order) != null &&
                        menu.findItem(R.id.action_sort_order).subMenu != null
                    ){
                        setUpSortOrderMenu(
                            menu.findItem(R.id.action_sort_order).subMenu!!
                        )
                    }
                }

                override fun onMenuItemSelected(
                    menuItem: MenuItem
                ): Boolean {
                    when(menuItem.itemId){
                        R.id.action_settings -> {
                            Toast.makeText(
                                requireContext(),
                                "Settings",
                                Toast.LENGTH_SHORT
                            ).show()

                            return true
                        }
                    }

                    return onSetSelectedItem(menuItem)
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
    }

    fun getSortOrder(): String?{
        return loadSortOrder()
    }

    fun itemLayoutRes(): Int {
        return if (getGridSize() == 1) {
            R.layout.item_list
        }
        else R.layout.item_grid
    }

    protected abstract fun setUpSortOrderMenu(sortOrderMenu: SubMenu)
    protected abstract fun setUpGridSizeMenu(gridSizeMenu: SubMenu)
    protected abstract fun onSetSelectedItem(item: MenuItem):Boolean
    protected abstract fun handleGridSizeMenuItem(item: MenuItem): Boolean
    protected abstract fun handleSortOrderMenuItem(item: MenuItem): Boolean
    protected abstract fun loadGridSize(): Int
    protected abstract fun loadSortOrder(): String?
    protected abstract fun setAndSaveGridSize(gridSize: Int)
    protected abstract fun setAndSaveSortOrder(sortOrder: String)


}