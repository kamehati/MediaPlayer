package com.example.elect.mediaplayer.adapter.music.song

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.db.MediaFavoriteEntity
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.fragments.base.BaseAlbumArtistDetailsRVFragment
import com.example.elect.mediaplayer.fragments.base.BaseToolbarFragment
import com.example.elect.mediaplayer.fragments.music.folder.SongFolderDetailsFragment
import com.example.elect.mediaplayer.fragments.music.song.SongFragment
import com.example.elect.mediaplayer.fragments.playlist.PlaylistDetailsFragment
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.helper.MediaMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

open class SongAdapter(
    override val activity: FragmentActivity,
    open var dataSet: MutableList<Media>,
    open var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    open val fragment: Fragment,
    open var cabHolderMenu: Int
    ): BaseMultiSelectAdapter<SongAdapter.ViewHolder, Media>(
    activity,
    ICabHolder,
    cabHolderMenu
),PopupTextProvider {
    private var listMediaFavoriteEntity: List<MediaFavoriteEntity> = listOf()

    init {
        this.setHasStableIds(true)
    }

    fun syncMediaFavoriteEntity(list: List<MediaFavoriteEntity>){
        listMediaFavoriteEntity = list.distinct()

        notifyDataSetChanged()
    }

    open fun swapDataSet(dataSet: List<Media>){
        this.dataSet = ArrayList(dataSet)
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        return dataSet[position].id
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view = LayoutInflater
            .from(activity)
            .inflate(
                itemLayoutRes,
                parent,
                false
            )

        return createdViewHolder(view)
    }

    private fun createdViewHolder(
        view: View
    ): ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val media = dataSet[position]

        val isChecked = isChecked(media)

        holder.itemView.isActivated = isChecked

        if(fragment is BaseToolbarFragment){
            val size = (fragment as BaseToolbarFragment).getGridSize()
            if(size >= 3){
                holder.menu?.hide()
                holder.favoriteView?.hide()
            } else {

                if (isChecked) {

                    holder.menu?.hide()
                    holder.favoriteView?.hide()
                }

                else {

                    holder.menu?.show()
                    holder.favoriteView?.show()
                }
            }
        }

        holder.favoriteView?.apply {
            setImageResource(
                GlideExtensions.DEFAULT_FAVORITE_FALSE
            )
            iconColor(R.color.md_white_1000)
        }

        for (favorite: MediaFavoriteEntity in listMediaFavoriteEntity){
            if(media.id == favorite.id){
                if(favorite.isFavorite){
                    holder.favoriteView?.setImageResource(
                        GlideExtensions.DEFAULT_FAVORITE_TRUE
                    )

                    holder.favoriteView?.iconColor(R.color.md_blue_grey_400)
                }
            }
        }

        holder.title?.text = getTitle(media)
        holder.text?.text = getSongText(media)

        loadAlbumCover(media, holder)
    }

    private fun getTitle(media: Media): String{
        return media.title
    }

    private fun getSongText(media: Media): String{
        return when (media.isSongOrVideo) {
            1 -> {
                MusicUtil.getReadableDurationString(
                    media.duration
                ) + " ・ " + media.artistName
            }
            2 -> {
                MusicUtil.getReadableDurationString(
                    media.duration
                ) + " ・ " + media.folderName
            }
            else -> {
                ""
            }
        }
    }

    protected open fun loadAlbumCover(
        media: Media,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }


        GlideApp.with(activity)
            .asBitmapPalette()
            .mediaCoverOptions(media)
            .load(
                GlideExtensions.getMediaModel(media)
            ).into(
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
                PreferenceUtil.songSortOrder
            ){
                SortOrder.SongSortOrder.SONG_A_Z,
                SortOrder.SongSortOrder.SONG_Z_A
                -> dataSet[position].title

                SortOrder.SongSortOrder.SONG_ALBUM
                -> dataSet[position].albumName

                SortOrder.SongSortOrder.SONG_ARTIST
                -> dataSet[position].artistName

                SortOrder.SongSortOrder.SONG_YEAR
                -> MusicUtil.getYearString(
                    dataSet[position].year
                )

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }

    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView) {

        protected open var mediaMenuRes =
            MediaMenuHelper.MENU_RES
        protected open val media : Media
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)

            imageContainer?.strokeColor(R.color.md_grey_900)

            favoriteView?.setOnClickListener{
                var count = 1
                var isFavorite = false

                for (mediaFavoriteEntity: MediaFavoriteEntity
                in listMediaFavoriteEntity){

                    if(media.id == mediaFavoriteEntity.id){
                        if(mediaFavoriteEntity.isFavorite){
                            count -= 1
                        }
                    }
                }

                if(count == 1){
                    isFavorite = true
                }else if( count == 0){
                    isFavorite = false
                }

                when (fragment) {
                    is SongFragment -> {
                        (fragment as SongFragment).favoriteClicked(
                            media,
                            isFavorite,
                            favoriteView
                        )
                    }
                    is SongFolderDetailsFragment -> {
                        (fragment as SongFolderDetailsFragment).favoriteClicked(
                            media,
                            isFavorite,
                            favoriteView
                        )
                    }
                    is PlaylistDetailsFragment -> {
                        (fragment as PlaylistDetailsFragment).favoriteClicked(
                            media,
                            isFavorite,
                            favoriteView
                        )
                    }
                    is BaseAlbumArtistDetailsRVFragment<*> -> {
                        (fragment as BaseAlbumArtistDetailsRVFragment<*>).favoriteClicked(
                            media,
                            isFavorite,
                            favoriteView
                        )
                    }
                }
            }

            menu?.setOnClickListener(
                object : MediaMenuHelper
                .OnClickMediaMenu(activity){
                    override val media : Media
                        get() = this@ViewHolder.media

                    override val menuRes: Int
                        get() = mediaMenuRes

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
                val activity = fragment.requireActivity()
                if(activity is MainActivity){
                    activity.selectShowBottom()
                }

                PlayerRemote.openQueue(
                    dataSet,
                    layoutPosition,
                    true
                )
            }
        }

        override fun onLongClick(v: View): Boolean {
            return toggleChecked(layoutPosition)
        }
    }

    override fun getIdentifier(position: Int): Media? {
        return dataSet[position]
    }

    override fun getName(i: Media): String {
        return i.title
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Media>
    ) {

        CabHolderMenuHelper.handleMenuClick(
            activity,
            selection,
            menuItem.itemId
        )
    }
}