package com.example.elect.mediaplayer.adapter.video

import android.net.Uri
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import com.bumptech.glide.Glide
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.base.BaseMultiSelectAdapter
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.glide.GlideExtensions.getMediaModel
import com.example.elect.mediaplayer.helper.CabHolderMenuHelper
import com.example.elect.mediaplayer.helper.SortOrder
import com.example.elect.mediaplayer.helper.VideoFolderMenuHelper
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.interfaces.IVideoFolderClickListener
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.model.VideoFolder
import com.example.elect.mediaplayer.util.MusicUtil
import com.example.elect.mediaplayer.util.PreferenceUtil
import me.zhanghai.android.fastscroll.PopupTextProvider
import java.io.File

class VideoFolderAdapter(
    override val activity: FragmentActivity,
    var dataSet: MutableList<VideoFolder>,
    var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    val listener: IVideoFolderClickListener?
): BaseMultiSelectAdapter<VideoFolderAdapter.ViewHolder, VideoFolder>(
    activity,
    ICabHolder,
    R.menu.cab_holder_menu
) , PopupTextProvider {
    init {
        this.setHasStableIds(true)
    }

    open fun swapDataSet(dataSet: List<VideoFolder>){
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
        val videoFolder = dataSet[position]


        val isChecked = isChecked(videoFolder)


        holder.itemView.isActivated = isChecked

        if (isChecked) {

            holder.menu?.hide()
        }

        else {

            holder.menu?.show()
        }

        holder.title?.text = getTitle(videoFolder)
        holder.text?.isVisible = false


        if(holder.imageContainer != null){
            ViewCompat.setTransitionName(
                holder.imageContainer!!,
                videoFolder.id.toString()
            )
        }else {
            ViewCompat.setTransitionName(
                holder.image!!,
                videoFolder.id.toString()
            )
        }

        loadAlbumCover(videoFolder,holder)
    }

    protected open fun loadAlbumCover(
        videoFolder: VideoFolder,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }

        GlideApp.with(activity)
            .asBitmapPalette()
            .folderCoverOptions()
            .load(
                getMediaModel(videoFolder.safeGetFirstVideo())
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

    private fun getTitle(videoFolder: VideoFolder): String{
        return videoFolder.folderName
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    override fun getPopupText(position: Int): String {
        val sectionName: String? =
            when(
                PreferenceUtil.videoFolderSortOrder
            ){
                SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_A_Z,
                SortOrder.VideoFolderSortOrder.VIDEO_FOLDER_Z_A
                -> dataSet[position].folderName

                else -> ""
            }
        return MusicUtil.getSectionName(sectionName)
    }


    open inner class ViewHolder(itemView: View)
        : MediaEntryViewHolder(itemView){

        protected open var videoFolderMenuRes =
            VideoFolderMenuHelper.MENU_RES

        protected open val videoFolder
            get() = dataSet[layoutPosition]

        init {
            mediaEntryViewHolder(itemView)


            imageContainer?.strokeColor(R.color.md_grey_900)

            menu?.setOnClickListener(
                object : VideoFolderMenuHelper
                .OnClickVideoFolderMenu(activity){

                    override val videoFolder: VideoFolder
                        get() = this@ViewHolder.videoFolder

                    override val menuRes: Int
                        get() = videoFolderMenuRes

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
                        listener?.onVideoFolderClick(
                            dataSet[layoutPosition].id,
                            it
                        )
                    }
                } else {
                    image?.let {
                        listener?.onVideoFolderClick(
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

    override fun getIdentifier(position: Int): VideoFolder? {
        return dataSet[position]
    }

    override fun getName(i: VideoFolder): String? {
        return i.folderName
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<VideoFolder>
    ) {
        val medias = mutableListOf<Media>()
        for (videoFolder in selection){
            medias.addAll(videoFolder.videos)
        }


        CabHolderMenuHelper.handleMenuClick(
            activity,
            medias,
            menuItem.itemId
        )
    }
}