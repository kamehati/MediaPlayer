package com.example.elect.mediaplayer.fragments.base

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.view.animation.*
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.databinding.FragmentMusicRecyclerViewBinding
import com.example.elect.mediaplayer.extensions.dip
import com.example.elect.mediaplayer.extensions.scrollerColor
import com.example.elect.mediaplayer.extensions.textAndBackgroundColor
import com.example.elect.mediaplayer.helper.PlayerRemote
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder

abstract class BaseMusicRVFragment
<A: RecyclerView.Adapter<*>, LM : RecyclerView.LayoutManager>(
    @LayoutRes layout: Int
) : BaseToolbarFragment(layout) {
    private var _binding: FragmentMusicRecyclerViewBinding? = null
    private val binding get() = _binding!!
    protected var adapter: A? = null
    protected var layoutManager: LM? = null

    val shuffleButton get() = binding.shuffleButton

    abstract val isShuffleVisible: Boolean

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)


        _binding = FragmentMusicRecyclerViewBinding.bind(view)


        postponeEnterTransition()

        view.doOnPreDraw {

            startPostponedEnterTransition()
        }

        initLayoutManager()

        initAdapter()

        setUpRecyclerView()

        binding.shuffleButton.isVisible = false



    }


    private fun initLayoutManager() {
        layoutManager = createLayoutManager()
    }

    private fun initAdapter(){
        if(createAdapter() != null){
            adapter = createAdapter()


            adapter?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver(){

                    override fun onChanged() {
                        super.onChanged()

                        checkIsEmpty()

                        checkForPadding()
                    }
                }
            )
        }
    }

    private fun checkForPadding() {
        binding.recyclerView.updatePadding(
            bottom = if(
                PlayerRemote.isPlaying ||
                PlayerRemote.playingQueue.isNotEmpty()
            ) {
                dip(R.dimen.mini_player_height_expanded)
            } else{
                dip(R.dimen.bottom_nav_height)
            }
        )
    }

    protected open val emptyMessage: Int
        @StringRes get() = R.string.empty

    private fun checkIsEmpty() {
        binding.emptyText.setText(emptyMessage)
        binding.empty.isVisible = adapter!!.itemCount == 0
    }

    private fun setUpRecyclerView() {

        binding.recyclerView.apply {
            layoutManager = this@BaseMusicRVFragment.layoutManager
            if(this@BaseMusicRVFragment.adapter != null){
                adapter = this@BaseMusicRVFragment.adapter
            }

            layoutAnimation = AnimationUtils
                .loadLayoutAnimation(
                    requireContext(),
                    R.anim.item_appearance
                )

            createFastScroller(this)
        }

        checkForPadding()
    }

    protected fun invalidateLayoutManager() {
        initLayoutManager()
        binding.recyclerView.layoutManager = layoutManager
    }

    protected fun invalidateAdapter() {
        initAdapter()
        checkIsEmpty()
        binding.recyclerView.adapter = adapter
    }


    protected open fun createFastScroller(
        recyclerView: RecyclerView
    ): FastScroller {
        val fastScroller = FastScrollerBuilder(recyclerView).useMd2Style()

        fastScroller.textAndBackgroundColor(
            requireContext(),
            R.color.md_blue_grey_200,
            R.color.md_blue_500
        )
        fastScroller.scrollerColor(
            requireContext(),
            R.color.md_blue_500
        )
        return fastScroller.build()
    }

    private fun initShuffleBtn(isShuffleVisible: Boolean){
        if(isShuffleVisible){
            binding.recyclerView.addOnScrollListener(
                object : RecyclerView.OnScrollListener(){
                    override fun onScrolled(
                        recyclerView: RecyclerView,
                        dx: Int,
                        dy: Int
                    ) {
                        super.onScrolled(recyclerView, dx, dy)


                        if (dy > 0) {

                            binding.shuffleButton.hide()

                        } else if (dy < 0) {

                            binding.shuffleButton.show()
                        }
                    }
                }
            )

            binding.shuffleButton.apply {
                setOnClickListener{
                    onShuffleClicked()
                }
            }
        }
        else{
            binding.shuffleButton.isVisible = false
        }
    }

    open fun onShuffleClicked() {}

    protected abstract fun createAdapter(): A?
    protected abstract fun createLayoutManager(): LM?

    override fun onSetSelectedItem(
        item: MenuItem
    ): Boolean {
        if(handleGridSizeMenuItem(item)){
            return true
        }

        if(handleSortOrderMenuItem(item)){
            return true
        }

        return false
    }

    val recyclerView : RecyclerView?
        get() {
            return if(_binding != null){
                binding.recyclerView
            } else {
                null
            }
        }


    val container : CoordinatorLayout?
    get() {
        return if (_binding != null){
            binding.root
        } else {
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}