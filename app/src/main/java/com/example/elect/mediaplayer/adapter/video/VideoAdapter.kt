package com.example.elect.mediaplayer.adapter.video

import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.db.FavoriteEntity
import com.example.elect.mediaplayer.db.MediaFavoriteEntity
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.fragments.base.BaseActivityFragment
import com.example.elect.mediaplayer.fragments.base.BaseAlbumArtistDetailsRVFragment
import com.example.elect.mediaplayer.fragments.music.folder.SongFolderDetailsFragment
import com.example.elect.mediaplayer.fragments.music.song.SongFragment
import com.example.elect.mediaplayer.fragments.playlist.PlaylistDetailsFragment
import com.example.elect.mediaplayer.fragments.video.VideoFolderDetailsFragment
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.glide.GlideExtensions.getMediaModel
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.helper.VideoMenuHelper
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.io.File

open class VideoAdapter(
    override val activity: FragmentActivity,
    open var dataSet: MutableList<Media>,
    ICabHolder: ICabHolder?,
    open val fragment: Fragment
): BaseMultiSelectAdapter<VideoAdapter.ViewHolder, Media>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
), PopupTextProvider {


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
                R.layout.item_list,
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
        val video = dataSet[position]


        val isChecked = isChecked(video)

        holder.itemView.isActivated = isChecked

        if (isChecked) {

            holder.menu?.hide()
            holder.favoriteView?.hide()
        }

        else {

            holder.menu?.show()
            holder.favoriteView?.show()
        }

        holder.favoriteView?.apply {
            setImageResource(
                GlideExtensions.DEFAULT_FAVORITE_FALSE
            )

            iconColor(R.color.md_white_1000)
        }

        for (favorite: MediaFavoriteEntity in listMediaFavoriteEntity){
            if(video.id == favorite.id){
                if(favorite.isFavorite){
                    holder.favoriteView?.setImageResource(
                        GlideExtensions.DEFAULT_FAVORITE_TRUE
                    )

                    holder.favoriteView?.iconColor(R.color.md_blue_grey_400)
                }
            }
        }

        holder.title?.text = getTitle(video)
        holder.text?.text = getVideoText(video)

        loadVideoCover(video, holder)
    }

    private fun getTitle(video: Media): String{
        return video.title
    }

    private fun getVideoText(video: Media): String{
        return MusicUtil.getReadableDurationString(
            video.duration
        ) + " ・ " + video.folderName
    }

    protected open fun loadVideoCover(
        video: Media,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }


        GlideApp.with(activity)
            .asBitmapPalette()
            .mediaCoverOptions(video)
            .load(getMediaModel(video))
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
                PreferenceUtil.videoFolderDetailsSortOrder
            ){
                SortOrder.VideoFolderDetailsSortOrder.VIDEO_A_Z,
                SortOrder.VideoFolderDetailsSortOrder.VIDEO_Z_A
                -> dataSet[position].title

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }

    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView) {

        protected open var videoMenuRes =
            VideoMenuHelper.MENU_RES
        protected open val video : Media
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)


            imageContainer?.strokeColor(R.color.md_grey_900)

            favoriteView?.setOnClickListener{
                var count = 1
                var isFavorite = false

                for (mediaFavoriteEntity: MediaFavoriteEntity
                in listMediaFavoriteEntity){

                    if(video.id == mediaFavoriteEntity.id){
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
                    is VideoFolderDetailsFragment -> {
                        (fragment as VideoFolderDetailsFragment).favoriteClicked(
                            video,
                            isFavorite,
                            favoriteView
                        )
                    }
                }
            }

            menu?.setOnClickListener(
                object : VideoMenuHelper
                .OnClickVideoMenu(activity){
                    override val video : Media
                        get() = this@ViewHolder.video

                    override val menuRes: Int
                        get() = videoMenuRes

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

    override fun getName(i: Media): String? {
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