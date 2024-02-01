package com.example.elect.mediaplayer.adapter.music.album

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.helper.AlbumMenuHelper
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.IAlbumClickListener
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Album
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class AlbumAdapter (
    override val activity: FragmentActivity,
    var dataSet: MutableList<Album>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val listener: IAlbumClickListener?
): BaseMultiSelectAdapter<AlbumAdapter.ViewHolder, Album>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
) , PopupTextProvider {
    init {
        this.setHasStableIds(true)
    }

    open fun swapDataSet(dataSet: List<Album>){
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
        val album = dataSet[position]

        val isChecked = isChecked(album)

        holder.itemView.isActivated = isChecked
        if (isChecked) {
            holder.menu?.hide()
        }

        else {

            holder.menu?.show()
        }

        holder.title?.text = getTitle(album)
        holder.text?.isVisible = false


        if(holder.imageContainer != null){
            ViewCompat.setTransitionName(
                holder.imageContainer!!,
                album.id.toString()
            )
        }else {
            ViewCompat.setTransitionName(
                holder.image!!,
                album.id.toString()
            )
        }

        loadAlbumCover(album,holder)
    }

    protected open fun loadAlbumCover(
        album: Album,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }

        val media = album.safeGetFirstMediaSong()


        GlideApp.with(activity)
            .asBitmapPalette()
            .albumCoverOptions(media)
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

    private fun getTitle(album: Album): String{
        return album.albumName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String? =
            when(
                PreferenceUtil.albumSortOrder
            ){
                SortOrder.AlbumSortOrder.ALBUM_A_Z,
                SortOrder.AlbumSortOrder.ALBUM_Z_A
                -> dataSet[position].albumName

                SortOrder.AlbumSortOrder.ALBUM_NUMBER_OF_SONGS
                -> dataSet[position].mediaCount.toString()

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }



    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView){

        protected open var albumMenuRes =
            AlbumMenuHelper.MENU_RES

        protected open val album
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)


            imageContainer?.strokeColor(R.color.md_grey_900)

            menu?.setOnClickListener(
                object : AlbumMenuHelper
                .OnClickAlbumMenu(activity){

                    override val album: Album
                        get() = this@ViewHolder.album

                    override val menuRes: Int
                        get() = albumMenuRes

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
                if(imageContainer != null){
                    imageContainer!!.let {
                        listener?.onAlbumClick(
                            dataSet[layoutPosition].id,
                            it
                        )
                    }
                } else {
                    image?.let {
                        listener?.onAlbumClick(
                            dataSet[layoutPosition].id,
                            it
                        )
                    }
                }
            }
        }

        override fun onLongClick(v: View): Boolean {
            return toggleChecked(layoutPosition)
        }
    }

    override fun getIdentifier(position: Int): Album? {
        return dataSet[position]
    }

    override fun getName(i: Album): String? {
        return i.albumName
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Album>
    ) {
        val medias = mutableListOf<Media>()
        for (album in selection){
            medias.addAll(album.medias)
        }


        CabHolderMenuHelper.handleMenuClick(
            activity,
            medias,
            menuItem.itemId
        )
    }
}