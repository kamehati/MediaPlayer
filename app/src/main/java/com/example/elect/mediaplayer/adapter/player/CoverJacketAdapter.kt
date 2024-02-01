package com.example.elect.mediaplayer.adapter.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.extensions.strokeColor
import com.example.elect.mediaplayer.glide.ColoredTarget
import com.example.elect.mediaplayer.glide.GlideApp
import com.example.elect.mediaplayer.glide.GlideExtensions
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.model.Song
import com.google.android.material.card.MaterialCardView

class CoverJacketAdapter(
    private val activity: FragmentActivity,
    private var dataSet: List<Media>
) : RecyclerView.Adapter<CoverJacketAdapter.ViewHolder>() {

    init {
        this.setHasStableIds(true)
    }

    fun swapDataSet(dataSet: List<Media>){
        this.dataSet = ArrayList(dataSet)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder = ViewHolder(
        LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.fragment_cover_jacket,
                parent,
                false
            )
    )

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val media = dataSet[position]

        loadCoverJacket(media, holder)
    }

    private fun loadCoverJacket(
        media: Media,
        holder: ViewHolder
    ){
        if(holder.image == null){
            return
        }

        if(holder.container != null){
            holder.container.strokeColor(R.color.md_grey_900)
        }

        GlideApp.with(activity)
            .asBitmapPalette()
            .mediaCoverOptions(media)
            .load(
                GlideExtensions.getMediaModel(media)
            ).into(
                object : ColoredTarget(holder.image){

                    init {
                        bitmapPaletteTarget(holder.image)
                    }

                    override fun onColorReady() {

                    }
                }
            )
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    inner class ViewHolder(itemView: View)
        : RecyclerView.ViewHolder(itemView){
        val image : ImageView? =
            itemView.findViewById(R.id.player_image)
        val container : MaterialCardView? =
            itemView.findViewById(R.id.player_image_container)
    }
}