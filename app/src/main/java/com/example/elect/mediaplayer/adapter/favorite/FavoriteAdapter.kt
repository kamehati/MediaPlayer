package com.example.elect.mediaplayer.adapter.favorite

import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.iconColor
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.fragments.favorite.FavoriteFragment
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.FavoriteMediaMenuHelper
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class FavoriteAdapter(
    override val activity: FragmentActivity,
    var dataSet: MutableList<Media>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val fragment: FavoriteFragment
): BaseMultiSelectAdapter<FavoriteAdapter.ViewHolder, Media>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
), PopupTextProvider {

    init {
        this.setHasStableIds(true)
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

        if (isChecked) {

            holder.menu?.hide()
            holder.favoriteView?.hide()
        }

        else {

            holder.menu?.show()
            holder.favoriteView?.show()
        }

        holder.title?.text = getTitle(media)
        holder.text?.text = getMediaText(media)

        holder.favoriteView?.setImageResource(
            GlideExtensions.DEFAULT_FAVORITE_TRUE
        )
        holder.favoriteView?.iconColor(R.color.md_blue_grey_400)
        
        loadAlbumCover(media, holder)
    }

    private fun getTitle(media: Media): String{
        return media.title
    }

    private fun getMediaText(media: Media): String{
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
                PreferenceUtil.favoriteSortOrder
            ){
                SortOrder.FavoriteSortOrder.FAVORITE_A_Z,
                SortOrder.FavoriteSortOrder.FAVORITE_Z_A
                -> dataSet[position].title

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }


    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView) {

        protected open var mediaMenuRes =
            FavoriteMediaMenuHelper.MENU_RES
        protected open val media : Media
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)

            imageContainer?.strokeColor(R.color.md_grey_900)

            favoriteView?.setOnClickListener{

                fragment.favoriteClicked(
                    media,
                    false,
                    favoriteView
                )
            }

            menu?.setOnClickListener(
                object : FavoriteMediaMenuHelper
                .OnClickMediaMenu(activity){
                    override val media: Media
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