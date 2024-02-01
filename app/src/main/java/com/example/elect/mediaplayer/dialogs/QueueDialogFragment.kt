package com.example.elect.mediaplayer.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.activity.PlayerActivity
import com.example.elect.mediaplayer.adapter.base.MediaEntryViewHolder
import com.example.elect.mediaplayer.adapter.music.song.SongAdapter
import com.example.elect.mediaplayer.databinding.BottomSheetDialogBinding
import com.example.elect.mediaplayer.extensions.currentFragment
import com.example.elect.mediaplayer.fragments.player.MusicPlayerFragment
import com.example.elect.mediaplayer.fragments.player.VideoPlayerFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.MusicUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QueueDialogFragment: BottomSheetDialogFragment() {

    private lateinit var binding: BottomSheetDialogBinding
    private lateinit var adapter: BottomDialogAdapter
    private lateinit var layoutManager: GridLayoutManager


    override fun getTheme(): Int {
        return R.style.BottomSheetDialogTheme
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomSheetDialogBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.queueText.text = "再生中のキュー"

        initLayoutManager()
        initAdapter()
        setUpRecyclerView()
    }

    private fun initLayoutManager() {
        layoutManager = GridLayoutManager(
            requireActivity(),
            1
        )
    }

    private fun initAdapter(){
        adapter = BottomDialogAdapter(
            requireActivity(),
            ArrayList(PlayerRemote.playingQueue)
        )
    }

    private fun setUpRecyclerView(){
        binding.queueRV.apply {
            layoutManager = this@QueueDialogFragment.layoutManager
            adapter = this@QueueDialogFragment.adapter
        }
    }

    companion object {
        fun newInstance(): QueueDialogFragment {
            val args = Bundle()
            val fragment = QueueDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    inner class BottomDialogAdapter(
        val activity: FragmentActivity,
        var dataSet: MutableList<Media>
    ): RecyclerView
    .Adapter<BottomDialogAdapter.ViewHolder>(){

        init {
            this.setHasStableIds(true)
        }

        fun swapDataSet(dataSet: List<Media>){
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
                    R.layout.item_queue,
                    parent,
                    false
                )

            return createdViewHolder(view)
        }

        private fun createdViewHolder(
            view: View
        ): BottomDialogAdapter.ViewHolder {
            return ViewHolder(view)
        }

        override fun onBindViewHolder(
            holder: ViewHolder,
            position: Int
        ) {
            val media = dataSet[position]



            holder.title?.text = getTitle(media)
            holder.text?.text = getSongText(media)
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

        override fun getItemCount(): Int {
            return dataSet.size
        }

        inner class ViewHolder(itemView: View)
            : MediaEntryViewHolder(itemView) {

            val media : Media
                get() = dataSet[layoutPosition]

            init {
                mediaEntryViewHolder(itemView)
            }


            override fun onClick(v: View) {

                PlayerRemote.openQueue(
                    dataSet,
                    layoutPosition,
                    true
                )

                val activity = activity as PlayerActivity
                val nowFragment = activity.currentFragment(R.id.playerFragmentContainer)
                if(nowFragment is VideoPlayerFragment){
                    if(media.isSongOrVideo == 1){
                        nowFragment.playerClose()
                        activity.createPlayerFragment(
                            media.isSongOrVideo
                        )
                    } else if(media.isSongOrVideo == 2){

                        PlayerRemote.pauseMedia()

                        nowFragment.playerClose()
                        nowFragment.createPlayer(
                            layoutPosition,
                            dataSet
                        )
                        nowFragment.playVideo()
                    }
                } else if(nowFragment is MusicPlayerFragment){

                }


                swapDataSet(PlayerRemote.playingQueue)
            }


            override fun onLongClick(v: View): Boolean {
                return true
            }
        }
    }
}