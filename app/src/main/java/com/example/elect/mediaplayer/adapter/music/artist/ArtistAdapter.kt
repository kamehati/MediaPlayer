package com.example.elect.mediaplayer.adapter.music.artist

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
import com.example.elect.mediaplayer.helper.ArtistMenuHelper
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.IArtistClickListener
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Artist
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class ArtistAdapter(
    override val activity: FragmentActivity,
    var dataSet: MutableList<Artist>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val listener: IArtistClickListener?
): BaseMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
) , PopupTextProvider {

    init {
        this.setHasStableIds(true)
    }

    open fun swapDataSet(dataSet: List<Artist>){
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
        val artist = dataSet[position]

        val isChecked = isChecked(artist)

        holder.itemView.isActivated = isChecked

        if (isChecked) {

            holder.menu?.hide()
        }
        else {
            holder.menu?.show()
        }

        holder.title?.text = getTitle(artist)
        holder.text?.isVisible = false

        if(holder.imageContainer != null){
            ViewCompat.setTransitionName(
                holder.imageContainer!!,
                artist.id.toString()
            )
        }else {
            ViewCompat.setTransitionName(
                holder.image!!,
                artist.id.toString()
            )
        }

        loadAlbumCover(artist,holder)
    }

    protected open fun loadAlbumCover(
        artist: Artist,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }

        val media = artist.safeGetFirstMedia()


        GlideApp.with(activity)
            .asBitmapPalette()
            .artistImageOptions(media)
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

    private fun getTitle(artist: Artist): String{
        return artist.artistName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String? =
            when(
                PreferenceUtil.artistSortOrder
            ){
                SortOrder.ArtistSortOrder.ARTIST_A_Z,
                SortOrder.ArtistSortOrder.ARTIST_Z_A
                -> dataSet[position].artistName

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }


    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView){

        protected open var artistMenuRes =
            ArtistMenuHelper.MENU_RES
        protected open val artist
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)

            imageContainer?.strokeColor(R.color.md_grey_900)

            menu?.setOnClickListener(
                object : ArtistMenuHelper
                .OnClickArtistMenu(activity){

                    override val artist: Artist
                        get() = this@ViewHolder.artist

                    override val menuRes: Int
                        get() = artistMenuRes

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
                        listener?.onArtistClick(
                            dataSet[layoutPosition].id,
                            it
                        )
                    }
                } else {
                    image?.let {
                        listener?.onArtistClick(
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

    override fun getIdentifier(position: Int): Artist? {
        return dataSet[position]
    }

    override fun getName(i: Artist): String? {
        return i.artistName
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Artist>
    ) {
        val medias = mutableListOf<Media>()
        for (artist in selection){
            medias.addAll(artist.medias)
        }


        CabHolderMenuHelper.handleMenuClick(
            activity,
            medias,
            menuItem.itemId
        )
    }
}