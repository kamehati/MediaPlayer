package com.example.elect.mediaplayer.adapter.playlist

import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.db.PlaylistEntity
import com.example.elect.mediaplayer.db.toMediaEntity
import com.example.elect.mediaplayer.db.toSongEntity
import com.example.elect.mediaplayer.dialogs.RemoveMediaFromPlaylistDialog
import com.example.elect.mediaplayer.extensions.hide
import com.example.elect.mediaplayer.extensions.show
import com.example.elect.mediaplayer.fragments.LibraryViewModel
import com.example.elect.mediaplayer.helper.MediaMenuHelper
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.example.elect.mediaplayer.util.MusicUtil
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PlaylistSongAdapter(
    override val activity: FragmentActivity,
    override var dataSet: MutableList<Media>,
    override var itemLayoutRes: Int,
    ICabHolder: ICabHolder?,
    override val fragment: Fragment,
    private val playlistEntity: PlaylistEntity
) : SongAdapter(
    activity, dataSet, itemLayoutRes, ICabHolder, fragment, R.menu.cab_playlist_song_menu
),
    DraggableItemAdapter
<PlaylistSongAdapter.ViewHolder>{

    val libraryViewModel: LibraryViewModel by activity.viewModel()

    inner class ViewHolder(
        itemView: View
    ): SongAdapter.ViewHolder(itemView){
        override var mediaMenuRes: Int
            get() = R.menu.menu_item_playlist_song
            set(value) {
                super.mediaMenuRes = value
            }

        init {
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
                        when(item.itemId){
                            R.id.action_delete_to_playlist_song -> {
                                RemoveMediaFromPlaylistDialog.create(
                                    media.toMediaEntity(playlistEntity.playlistId)
                                ).show(
                                    activity.supportFragmentManager,
                                    "REMOVE_FROM_PLAYLIST"
                                )
                                return true
                            }
                        }

                        return super.onMenuItemClick(item)
                    }
                }
            )
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PlaylistSongAdapter.ViewHolder {
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
    ): PlaylistSongAdapter.ViewHolder {
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SongAdapter.ViewHolder,
        position: Int
    ) {
        super.onBindViewHolder(holder, position)

        holder.dragView?.show()
    }



    override fun onCheckCanStartDrag(
        holder: ViewHolder,
        position: Int,
        x: Int,
        y: Int
    ): Boolean {
        if (dataSet.size == 0 || isInQuickSelectMode) {
            return false
        }


        val dragHandle = holder.dragView ?: return false

        val handleWidth = dragHandle.width

        val handleHeight = dragHandle.height

        val handleLeft = dragHandle.left

        val handleTop = dragHandle.top


        return (
                x >= handleLeft &&
                x < handleLeft + handleWidth &&
                y >= handleTop &&
                y < handleTop + handleHeight
        )
    }


    override fun onGetItemDraggableRange(
        holder: ViewHolder,
        position: Int
    ): ItemDraggableRange? {
        return ItemDraggableRange(0, itemCount - 1)
    }

    override fun onMoveItem(
        fromPosition: Int,
        toPosition: Int
    ) {
        dataSet.add(

            toPosition,

            dataSet.removeAt(fromPosition)
        )
    }


    override fun onCheckCanDrop(
        draggingPosition: Int,
        dropPosition: Int
    ): Boolean {
        return true
    }


    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }


    override fun onItemDragFinished(
        fromPosition: Int,
        toPosition: Int,
        result: Boolean
    ) {
        notifyDataSetChanged()
    }

    override fun getPopupText(position: Int): String {
        val sectionName : String? = dataSet[position].title

        return MusicUtil.getSectionName(sectionName)
    }


    fun saveSongs(playlistEntity: PlaylistEntity) {
        activity
            .lifecycleScope
            .launch(Dispatchers.IO) {

                libraryViewModel.insertMedias(

                    dataSet.toMediaEntity(playlistEntity)
                )
            }
    }

    override fun onMultipleItemAction(
        menuItem: MenuItem,
        selection: List<Media>
    ) {
        when(menuItem.itemId){
            R.id.action_delete_to_playlist_song -> {
                RemoveMediaFromPlaylistDialog.create(
                    selection.toMediaEntity(playlistEntity)
                ).show(
                    activity.supportFragmentManager,
                    "REMOVE_FROM_PLAYLIST"
                )
            }

            else -> super.onMultipleItemAction(menuItem, selection)
        }
    }
}