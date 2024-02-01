package com.example.elect.mediaplayer.fragments.video

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.transition.TransitionManager
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.EXTRA_VIDEO_FOLDER_ID
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.video.VideoFolderAdapter
import com.example.elect.mediaplayer.extensions.getIntRes
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseRVFragment
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.interfaces.IVideoFolderClickListener
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import com.google.android.material.transition.MaterialFade

class VideoFolderFragment
    : BaseRVFragment<VideoFolderAdapter, GridLayoutManager>
    (R.layout.fragment_recycler_view),
    IVideoFolderClickListener,
    ICabHolder {

    private var cab: AttachedCab? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(resources.getString(R.string.video))

        libraryViewModel
            .getVideoFolders()
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

    override fun createAdapter(): VideoFolderAdapter? {
        val dataSet =
            if(adapter == null) mutableListOf()
            else adapter!!.dataSet

        return VideoFolderAdapter(
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
            R.id.action_video_folder_grid_size_1,
            0,
            getIntRes(R.integer.grid_1).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_1)

        gridSizeMenu.add(
            0,
            R.id.action_video_folder_grid_size_2,
            1,
            getIntRes(R.integer.grid_2).toString()
        ).isChecked =
            currentGridSize == getIntRes(R.integer.grid_2)

        gridSizeMenu.add(
            0,
            R.id.action_video_folder_grid_size_3,
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

            R.id.action_video_folder_sort_order_asc,

            0,

            R.string.sort_order_a_z
        ).isChecked =

            currentSortOrder == SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_A_Z


        sortOrderMenu.add(
            0,
            R.id.action_video_folder_sort_order_desc,
            1,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_Z_A


        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        val gridSize = when(item.itemId){
            R.id.action_video_folder_grid_size_1 -> getIntRes(R.integer.grid_1)
            R.id.action_video_folder_grid_size_2 -> getIntRes(R.integer.grid_2)
            R.id.action_video_folder_grid_size_3 -> getIntRes(R.integer.grid_3)
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
    ): Boolean{
        val sortOrder: String = when(item.itemId){
            R.id.action_video_folder_sort_order_asc ->
                SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_A_Z
            R.id.action_video_folder_sort_order_desc ->
                SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_Z_A
            else -> PreferenceUtil.videoFolderSortOrder
        }

        if(sortOrder != PreferenceUtil.videoFolderSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadGridSize(): Int {
        return PreferenceUtil.videoFolderGridSize
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.videoFolderSortOrder
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        PreferenceUtil.videoFolderGridSize = gridSize

        invalidateLayoutManager()
        invalidateAdapter()

        recyclerView?.startLayoutAnimation()

        scrollToTop()
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.videoFolderSortOrder = sortOrder

        libraryViewModel.forceReload(ReloadType.VideoFolders)
    }

    override fun onVideoFolderClick(videoId: Long, view: View) {

        PreferenceUtil.appbarMode = 1
        findNavController().navigate(
            R.id.action_videoDetails,
            bundleOf(EXTRA_VIDEO_FOLDER_ID to videoId),
            null,

            FragmentNavigatorExtras(

                view to videoId.toString()
            )
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
}