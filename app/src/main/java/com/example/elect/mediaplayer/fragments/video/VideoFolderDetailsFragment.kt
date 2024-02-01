package com.example.elect.mediaplayer.fragments.video

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import android.widget.ImageView
import androidx.activity.addCallback
import androidx.core.view.doOnPreDraw
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.bumptech.glide.Glide
import com.bumptech.glide.load.MultiTransformation
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.adapter.video.VideoAdapter
import com.example.elect.mediaplayer.adapter.video.VideoFolderDetailsAdapter
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.fragments.ReloadType
import com.example.elect.mediaplayer.fragments.base.BaseFolderDetailsRVFragment
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.VideoFolder
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.service.MusicService
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import jp.wasabeef.glide.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf
import java.io.File

class VideoFolderDetailsFragment :
    BaseFolderDetailsRVFragment<VideoFolderDetailsAdapter>(
        R.layout.fragment_folder_details
    ), ICabHolder {

    private var cab: AttachedCab? = null

    private val arguments by navArgs<VideoFolderDetailsFragmentArgs>()
    private val detailsViewModel by viewModel<VideoFolderDetailsViewModel>{
        parametersOf(arguments.extraVideoFolderId)
    }

    private var videoFolder: VideoFolder? = null

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(resources.getString(R.string.folder))
        setText(R.string.video)

        detailsViewModel
            .getVideoFolder()
            .observe(viewLifecycleOwner){
                requireView().doOnPreDraw {
                    startPostponedEnterTransition()
                }

                showVideoFolder(it)

                appBarLayoutTransition(it.id.toString())
            }

        libraryViewModel.getMediaFavoriteEntities()
            .observe(viewLifecycleOwner){
                if (adapterFolder is VideoAdapter){
                    if(it.isNotEmpty())
                        adapterFolder?.syncMediaFavoriteEntity(it)
                    else
                        adapterFolder?.syncMediaFavoriteEntity(listOf())
                }
            }


        postponeEnterTransition()
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

    override fun onResume() {
        super.onResume()


        binding.appBarLayout.setCollapsingColor(R.color.md_blue_grey_900)
        binding.appBarLayout.setTextColor(R.color.md_white_1000)


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

    override fun returnMedias(): List<Media> {
        return videoFolder?.videos ?: listOf(Media.emptyMedia)
    }


    private fun handleBackPress(): Boolean {
        cab?.let {

            if (it.isActive()) {

                it.destroy()
                setToolbarTitle(resources.getString(R.string.folder))
                return true
            }
        }
        return false
    }

    private fun showVideoFolder(videoFolder: VideoFolder) {
        if (videoFolder.videos.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        this.videoFolder = videoFolder

        binding.fragmentContent.title.text = videoFolder.folderName

        binding.fragmentContent.text.text = String.format(
            "%s 　 %s",
            resources.getString(R.string.x_all_video_details, videoFolder.videos.size),
            MusicUtil.getReadableDurationString(
                MusicUtil.getTotalMediaDuration(videoFolder.videos)
            )
        )

        val multi = MultiTransformation(
            BlurTransformation(15)
        )

        val file = File(videoFolder.safeGetFirstVideo().data)
        val artUri = Uri.fromFile(file)
        binding.appBarLayout.image?.let {
            Glide.with(it.context)
                .load(
                    artUri
                )
                .transform(multi)
                .error(GlideExtensions.DEFAULT_FOLDER_IMAGE)
                .placeholder(GlideExtensions.DEFAULT_FOLDER_IMAGE)
                .into(it)
        }

        adapterFolder?.swapDataSet(videoFolder.videos)
    }

    override fun createAdapter(): VideoFolderDetailsAdapter? {
        return VideoFolderDetailsAdapter(
            mainActivity,
            ArrayList(),
            this,
            this
        )
    }

    override fun openCab(
        menuRes: Int,
        callback: ICabCallback
    ): AttachedCab {
        cab?.let {

            if (it.isActive()) {
                it.destroy()
                setToolbarTitle(resources.getString(R.string.folder))
            }
        }


        cab = createCab(R.id.toolbar_container) {

            menu(menuRes)

            closeDrawable(R.drawable.ic_round_pause)

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

        setToolbarTitle(resources.getString(R.string.space))
        return cab as AttachedCab
    }

    override fun onPause() {
        super.onPause()
        if (cab.isActive()) {
            cab.destroy()
            setToolbarTitle(resources.getString(R.string.folder))
        }
    }

    override fun selectedAddPlaylist(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_add_to_playlist){
            CoroutineScope(Dispatchers.IO)
                .launch {
                    val playlists = get<RealRepository>()
                        .fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        adapterFolder?.let {
                            AddToPlaylistDialog
                                .create(
                                    playlists,
                                    mutableListOf(),
                                    mainActivity
                                ).show(
                                    mainActivity.supportFragmentManager,
                                    "ADD_PLAYLIST"
                                )
                        }
                    }
                }
        }

        return true
    }

    override fun setUpSortOrderMenu(sortOrderMenu: SubMenu) {
        val currentSortOrder: String? = getSortOrder()
        sortOrderMenu.clear()

        sortOrderMenu.add(
            0,
            R.id.action_video_folder_details_sort_order_asc,
            0,
            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_video_folder_details_sort_order_desc,
            1,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.VideoFolderDetailsSortOrder.VIDEO_Z_A

        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        val sortOrder: String = when(item.itemId){
            R.id.action_video_folder_details_sort_order_asc ->
                SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z
            R.id.action_video_folder_details_sort_order_desc ->
                SortOrder.VideoFolderDetailsSortOrder.VIDEO_Z_A
            else -> PreferenceUtil.videoFolderDetailsSortOrder
        }

        if(sortOrder != PreferenceUtil.videoFolderDetailsSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.videoFolderDetailsSortOrder
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.videoFolderDetailsSortOrder = sortOrder

        detailsViewModel.forceReload()
    }
}