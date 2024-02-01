package com.example.elect.mediaplayer.fragments.music.album

import android.os.Bundle
import android.view.MenuItem
import android.view.SubMenu
import android.view.View
import androidx.activity.addCallback
import androidx.core.view.doOnPreDraw
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.afollestad.materialcab.createCab
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.album.AlbumSongAdapter
import com.example.elect.mediaplayer.dialogs.AddToPlaylistDialog
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.fragments.base.BaseAlbumArtistDetailsRVFragment
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions.getMediaModel
import com.example.elect.mediaplayer.glide.GlideExtensions.getSongModel
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.repository.RealRepository
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.color.MaterialColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.get
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class AlbumDetailsFragment :
    BaseAlbumArtistDetailsRVFragment<AlbumSongAdapter>(
        R.layout.fragment_album_artist_details
    ), ICabHolder {

    private var cab: AttachedCab? = null

    private val arguments by navArgs<AlbumDetailsFragmentArgs>()
    private val detailsViewModel by viewModel<AlbumDetailsViewModel>{
        parametersOf(arguments.extraAlbumId)
    }
    private lateinit var album: Album

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        setToolbarTitle(resources.getString(R.string.album))


        postponeEnterTransition()

        detailsViewModel
            .getAlbum()
            .observe(viewLifecycleOwner){
                requireView().doOnPreDraw {
                    startPostponedEnterTransition()
                }

                showAlbum(it)
            }

        libraryViewModel
            .getMediaFavoriteEntities()
            .observe(viewLifecycleOwner){
                if(it.isNotEmpty())
                    adapterALAR?.syncMediaFavoriteEntity(it)
                else
                    adapterALAR?.syncMediaFavoriteEntity(listOf())
            }
    }

    override fun onResume() {
        super.onResume()

        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                if (!handleBackPress()) {
                    remove()
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

    private fun showAlbum(album: Album) {
        if (album.medias.isEmpty()) {
            findNavController().navigateUp()
            return
        }
        this.album = album

        binding.fragmentContent.title.text = album.albumName
        val songText = resources.getQuantityString(

            R.plurals.albumSongs,

            album.mediaCount,
            album.mediaCount
        )
        binding.fragmentContent.songTitle.text = songText

        binding.fragmentContent.text.text = String.format(
            "%s   %s",
            resources.getString(R.string.x_all_song_details, album.medias.size),
            MusicUtil.getReadableDurationString(
                MusicUtil.getTotalDuration(album.medias)
            )
        )

        loadAlbumCover(album)

        adapterALAR?.swapDataSet(album.medias)
    }

    private fun loadAlbumCover(album: Album) {

        binding.coverContainer.strokeColor(R.color.md_grey_900)

        GlideApp.with(requireContext()).asBitmapPalette()
            .albumCoverOptions(album.safeGetFirstMediaSong())

            .load(getMediaModel(album.safeGetFirstMediaSong()))
            .into(
                object : ColoredTarget(binding.image) {

                    override fun onColorReady() {

                    }
                }
            )
    }

    override fun returnMedias(): List<Media> {
        return album.medias
    }

    override fun setTransitionId(): String {
        return arguments.extraAlbumId.toString()
    }

    override fun createAdapter(): AlbumSongAdapter? {
        return AlbumSongAdapter(
            mainActivity,
            ArrayList(),
            R.layout.item_list,
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

    override fun selectedAddPlaylist(item: MenuItem): Boolean {
        if(item.itemId == R.id.action_add_to_playlist){
            CoroutineScope(Dispatchers.IO)
                .launch {
                    val playlists = get<RealRepository>()
                        .fetchPlaylists()
                    withContext(Dispatchers.Main) {
                        adapterALAR?.let {
                            AddToPlaylistDialog
                                .create(
                                    playlists,
                                    it.dataSet,
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
            R.id.action_album_details_sort_order_asc,
            0,
            R.string.sort_order_a_z
        ).isChecked =
            currentSortOrder == SortOrder.AlbumDetailsSortOrder.SONG_A_Z

        sortOrderMenu.add(
            0,
            R.id.action_album_details_sort_order_desc,
            1,
            R.string.sort_order_z_a
        ).isChecked =
            currentSortOrder == SortOrder.AlbumDetailsSortOrder.SONG_Z_A

        sortOrderMenu.setGroupCheckable(0,true, true)
    }

    override fun handleSortOrderMenuItem(item: MenuItem): Boolean {
        val sortOrder: String = when(item.itemId){
            R.id.action_album_details_sort_order_asc ->
                SortOrder.AlbumDetailsSortOrder.SONG_A_Z
            R.id.action_album_details_sort_order_desc ->
                SortOrder.AlbumDetailsSortOrder.SONG_Z_A
            else -> PreferenceUtil.albumDetailsSortOrder
        }

        if(sortOrder != PreferenceUtil.albumDetailsSortOrder){
            item.isChecked = true
            setAndSaveSortOrder(sortOrder)

            return true
        }
        return false
    }

    override fun loadSortOrder(): String? {
        return PreferenceUtil.albumDetailsSortOrder
    }

    override fun setAndSaveSortOrder(sortOrder: String) {
        PreferenceUtil.albumDetailsSortOrder = sortOrder

        detailsViewModel.forceReload()
    }
}