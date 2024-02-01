package com.example.elect.mediaplayer.adapter.music.folder

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
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.SongFolderMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.interfaces.ISongFolderClickListener
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.model.SongFolder
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider

class SongFolderAdapter (
    override val activity: FragmentActivity,
    var dataSet: MutableList<SongFolder>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val listener: ISongFolderClickListener?
): BaseMultiSelectAdapter<SongFolderAdapter.ViewHolder, SongFolder>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
), PopupTextProvider {


    init {
        this.setHasStableIds(true)
    }

    open fun swapDataSet(dataSet: List<SongFolder>){
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
        val songFolder = dataSet[position]

        val isChecked = isChecked(songFolder)

        holder.itemView.isActivated = isChecked
        if (isChecked) {

            holder.menu?.hide()
        }

        else {

            holder.menu?.show()
        }

        holder.title?.text = getTitle(songFolder)
        holder.text?.isVisible = false

        if(holder.imageContainer != null){
            ViewCompat.setTransitionName(
                holder.imageContainer!!,
                songFolder.id.toString()
            )
        }else {
            ViewCompat.setTransitionName(
                holder.image!!,
                songFolder.id.toString()
            )
        }

        loadAlbumCover(songFolder,holder)
    }

    protected open fun loadAlbumCover(
        songFolder: SongFolder,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }


        GlideApp.with(activity)
            .asBitmapPalette()
            .folderCoverOptions()
            .load(
                GlideExtensions.getMediaModel(
                    songFolder.safeGetFirstMediaSong()
                )
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

    private fun getTitle(songFolder: SongFolder): String{
        return songFolder.folderName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String? =
            when(
                PreferenceUtil.songFolderSortOrder
            ){
                SortOrder.SongFolderSortOrder.SONG_FOLDER_A_Z,
                SortOrder.SongFolderSortOrder.SONG_FOLDER_Z_A
                -> dataSet[position].folderName

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }



    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView){

        protected open var songFoldrMenuRes =
            SongFolderMenuHelper.MENU_RES

        protected open val songFolder
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)


            imageContainer?.strokeColor(R.color.md_grey_900)

            menu?.setOnClickListener(
                object : SongFolderMenuHelper
                .OnClickSongFolderMenu(activity){

                    override val songFolder: SongFolder
                        get() = this@ViewHolder.songFolder

                    override val menuRes: Int
                        get() = songFoldrMenuRes

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
                        listener?.onSongFolderClick(
                            dataSet[layoutPosition].id,
                            it
                        )
                    }
                } else {
                    image?.let {
                        listener?.onSongFolderClick(
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

    override fun getIdentifier(position: Int): SongFolder? {
        return dataSet[position]
    }

    override fun getName(i: SongFolder): String? {
        return i.folderName
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<SongFolder>
    ) {
        val medias = mutableListOf<Media>()
        for (songFolder in selection){
            medias.addAll(songFolder.medias)
        }


        CabHolderMenuHelper.handleMenuClick(
            activity,
            medias,
            menuItem.itemId
        )
    }
}