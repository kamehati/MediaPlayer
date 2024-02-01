package com.example.elect.mediaplayer.adapter.base

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.MenuRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialcab.attached.AttachedCab
import com.afollestad.materialcab.attached.destroy
import com.afollestad.materialcab.attached.isActive
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.interfaces.ICabCallback
import com.example.elect.mediaplayer.interfaces.ICabHolder
import com.example.elect.mediaplayer.util.BuildUtil

abstract class BaseMultiSelectAdapter
<V:RecyclerView.ViewHolder, I>(
    open val activity: FragmentActivity,
    private val ICabHolder: ICabHolder?,
    @MenuRes menuRes: Int
    ) :RecyclerView.Adapter<V>(),
    ICabCallback {

    private var cab: AttachedCab? = null
    private val checked: MutableList<I>
    private var menuRes: Int

    init {
        checked = ArrayList()
        this.menuRes = menuRes
    }

    override fun onCabCreated(
        cab: AttachedCab,
        menu: Menu
    ): Boolean {

        activity.window.statusBarColor =
            ContextCompat.getColor(
                activity,
                R.color.md_blue_grey_600
            )
        return true
    }

    override fun onCabFinished(cab: AttachedCab)
            : Boolean {

        clearChecked()

        activity.window.statusBarColor = ContextCompat.getColor(
            activity,
            R.color.md_blue_grey_900
        )
        return true
    }

    override fun onCabItemClicked(item: MenuItem): Boolean {

        if (item.itemId ==

            R.id.action_multi_select_all_check) {

            checkAll()
        } else {

            onMultipleItemAction(item, ArrayList(checked))

            cab?.destroy()

            clearChecked()
        }
        return true
    }

    private fun checkAll() {

        if (ICabHolder != null) {

            checked.clear()

            for (i in 0 until itemCount) {

                val identifier = getIdentifier(i)

                if (identifier != null) {

                    checked.add(identifier)
                }
            }

            notifyDataSetChanged()

            updateCab()
        }
    }

    protected abstract fun getIdentifier(position: Int): I?

    protected open fun getName(i: I): String? {
        return i.toString()
    }


    protected fun isChecked(identifier: I): Boolean {
        return checked.contains(identifier)
    }

    protected val isInQuickSelectMode: Boolean
        get() = cab != null && cab!!.isActive()

    protected abstract fun onMultipleItemAction(
        menuItem: MenuItem, selection: List<I>
    )

    protected fun setMultiSelectMenuRes(
        @MenuRes menuRes: Int
    ) {
        this.menuRes = menuRes
    }

    protected fun toggleChecked(position: Int): Boolean {

        if (ICabHolder != null) {

            val identifier = getIdentifier(position) ?: return false

            if (!checked.remove(identifier)) {
                checked.add(identifier)
            }

            notifyItemChanged(position)

            updateCab()
            return true
        }
        return false
    }

    private fun clearChecked() {

        checked.clear()

        notifyDataSetChanged()
    }


    @SuppressLint("StringFormatInvalid", "StringFormatMatches")
    private fun updateCab() {

        if (ICabHolder != null) {

            if (cab == null || !cab!!.isActive()) {

                cab = ICabHolder.openCab(menuRes, this)
            }

            val size = checked.size
            when {
                size <= 0 -> {
                    cab?.destroy()
                }
                size == 1 -> {

                    cab?.title(literal = getName(checked[0]))
                }
                else -> {
                    cab?.title(literal = activity.getString(R.string.x_selected, size))
                }
            }
        }
    }
}