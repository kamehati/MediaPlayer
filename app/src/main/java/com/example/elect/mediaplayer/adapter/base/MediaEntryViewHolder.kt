package com.example.elect.mediaplayer.adapter.base

import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageView
import com.example.elect.mediaplayer.R
import com.google.android.material.card.MaterialCardView
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder

open class MediaEntryViewHolder(itemView: View)
    : AbstractDraggableSwipeableItemViewHolder(itemView),
    View.OnClickListener,
    View.OnLongClickListener
{

    var dragView: View? = null
    var favoriteView: ImageView? = null
    var image: ImageView? = null
    var imageContainer: MaterialCardView? = null
    var menu: AppCompatImageView? = null
    var text: TextView? = null
    var title: TextView? = null

    fun mediaEntryViewHolder(itemView: View){
        super.itemView

        title = itemView.findViewById(R.id.title)
        text = itemView.findViewById(R.id.text)

        image = itemView.findViewById(R.id.image)
        imageContainer = itemView.findViewById(R.id.imageContainer)

        menu = itemView.findViewById(R.id.menu)
        dragView = itemView.findViewById(R.id.drag_view)

        favoriteView = itemView.findViewById(R.id.favorite_view)

        itemView.setOnClickListener(this)
        itemView.setOnLongClickListener(this)
    }

    override fun getSwipeableContainerView(): View {
        TODO("Not yet implemented")
    }

    override fun onClick(v: View) {
        return
    }

    override fun onLongClick(v: View): Boolean {
        return true
    }

}