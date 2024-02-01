package com.example.elect.mediaplayer.adapter.playlist

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.db.*
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.PlaylistMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.interfaces.IPlaylistClickListener
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class PlaylistAdapter(
    override val activity: FragmentActivity,
    var dataSet: List<PlaylistWithMedias>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val listener: IPlaylistClickListener?
): BaseMultiSelectAdapter<PlaylistAdapter.ViewHolder, PlaylistWithMedias>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
), PopupTextProvider {
    init {
        setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<PlaylistWithMedias>) {
        this.dataSet = dataSet
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].playlistEntity.playlistId
    }

    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): ViewHolder {
        val view = LayoutInflater
            .from(activity)
            .inflate(
                itemLayoutRes,
                parent,
                false
            )

        return createViewHolder(view)
    }

    fun createViewHolder(view: View): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val playlist = dataSet[position]


        val isChecked = isChecked(playlist)

        holder.itemView.isActivated = isChecked
        if (isChecked) {

            holder.menu?.hide()
        }
        else {
            holder.menu?.show()
        }

        holder.title?.text =
            getPlaylistTitle(playlist.playlistEntity)
        holder.text?.text =
            getPlaylistText(playlist)

        val firstMedia = playlist.medias.firstOrNull()?.toMedia() ?: Media.emptyMedia

        loadAlbumCover(holder, firstMedia)
    }

    private fun getPlaylistTitle(
        playlist: PlaylistEntity
    ): String {
        return if (
            TextUtils.isEmpty(playlist.playlistName)
        ) "-"
        else playlist.playlistName
    }

    private fun getPlaylistText(
        playlist: PlaylistWithMedias
    ): String {
        return MusicUtil.getPlaylistInfoString(
            activity,
            playlist.medias.toMedias()
        )
    }

    protected open fun loadAlbumCover(
        holder: ViewHolder,
        media: Media
    ){
        if(holder.image == null){
            return
        }


        GlideApp.with(activity)
            .asBitmapPalette()
            .playlistOptions()
            .load(GlideExtensions.getMediaModel(media))
            .into(
                object : ColoredTarget(holder.image!!){

                    init {
                        bitmapPaletteTarget(holder.image!!)
                    }

                    override fun onColorReady() {

                    }
                }
            )
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String? =
            when(
                PreferenceUtil.playlistSortOrder
            ){
                SortOrder.PlaylistSortOrder.PLAYLIST_A_Z,
                SortOrder.PlaylistSortOrder.PLAYLIST_Z_A
                -> dataSet[position].playlistEntity.playlistName

                SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_ASC,
                SortOrder.PlaylistSortOrder.PLAYLIST_SONG_COUNT_DESC
                -> dataSet[position].medias.size.toString()

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }


    inner class ViewHolder(itemView: View) :
        MediaEntryViewHolder(itemView) {

        protected open var playlistMenuRes =
            PlaylistMenuHelper.MENU_RES
        protected open val playlistWithMedias
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)

            imageContainer?.strokeColor(R.color.md_grey_900)

            menu?.setOnClickListener(
                object : PlaylistMenuHelper
                .OnClickPlaylistMenu(activity){

                    override val menuRes: Int
                        get() = playlistMenuRes
                    override val playlist: PlaylistWithMedias
                        get() = this@ViewHolder.playlistWithMedias

                    override fun onMenuItemClick(
                        item: MenuItem
                    ): Boolean {
                        return super.onMenuItemClick(item)
                    }
                }
            )
        }

        override fun onClick(v: View) {

            if (isInQuickSelectMode) {
                toggleChecked(layoutPosition)
            } else {
                listener?.onPlaylistClick(
                    playlistWithMedias
                )
            }
        }

        override fun onLongClick(v: View): Boolean {
            return toggleChecked(layoutPosition)
        }
    }

    override fun getIdentifier(position: Int): PlaylistWithMedias? {
        return dataSet[position]
    }

    override fun getName(i: PlaylistWithMedias): String? {
        return i.playlistEntity.playlistName
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<PlaylistWithMedias>
    ) {
        val medias = mutableListOf<Media>()
        for (playlist in selection){
            medias.addAll(playlist.medias.toMedias())
        }


        CabHolderMenuHelper.handleMenuClick(
            activity,
            medias,
            menuItem.itemId
        )
    }
}