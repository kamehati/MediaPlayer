package com.example.elect.mediaplayer.fragments.base

import android.os.Bundle
import android.view.*
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.core.view.*
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.MainActivity
import com.example.elect.mediaplayer.databinding.FragmentFolderDetailsBinding
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.model.Media
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.transition.MaterialArcMotion
import com.google.android.material.transition.MaterialContainerTransform
import me.zhanghai.android.fastscroll.FastScroller
import me.zhanghai.android.fastscroll.FastScrollerBuilder

abstract class BaseFolderDetailsRVFragment
<A: RecyclerView.Adapter<*>>(
    @LayoutRes layout: Int
): BaseToolbarFragment(layout) {

    private var _binding: FragmentFolderDetailsBinding? = null
    val binding get() = _binding!!

    protected var adapterFolder: A? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        val transition = MaterialContainerTransform().apply {
            drawingViewId = R.id.fragment_container
            scrimColor = android.graphics.Color.TRANSPARENT
            setAllContainerColors(android.graphics.Color.TRANSPARENT)
            setPathMotion(MaterialArcMotion())
        }

        sharedElementEnterTransition = transition

        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        _binding = FragmentFolderDetailsBinding.bind(view)

        super.onViewCreated(view, savedInstanceState)

        setAppBarLayout()

        setUpRecyclerView()

        binding.appBarLayout.toolbar.apply {

            setNavigationOnClickListener {
                PreferenceUtil.appbarMode = 0
                findNavController().navigateUp()
            }


            setNavigationIcon(
                R.drawable.ic_round_keyboard_backspace
            )
        }

        binding.fragmentRVLayout.backgroundColor(R.color.md_grey_900)
        setActionClickListener()
    }


    fun appBarLayoutTransition(transitionId: String){

        binding.appBarLayout.image?.let {
            ViewCompat.setTransitionName(
                it,
                transitionId
            )
        }


        if(binding.appBarLayout.image == null){

        }
    }

    private fun setActionClickListener(){
        binding.fragmentContent.playAction.apply {
            setOnClickListener {
                val activity = requireActivity()
                if(activity is MainActivity){
                    activity.selectShowBottom()
                }
                PlayerRemote.openQueue(
                    returnMedias(),
                    0,
                    true
                )
            }

            textColor(R.color.md_blue_grey_200)
            backgroundColor(R.color.md_grey_900)
            strokeColor(R.color.md_blue_grey_200)
            iconColor(R.color.md_blue_500)
            rippleColor(R.color.md_blue_grey_200)
        }
        binding.fragmentContent.shuffleAction.apply {
            setOnClickListener {
                val activity = requireActivity()
                if(activity is MainActivity){
                    activity.selectShowBottom()
                }
                PlayerRemote.openAndShuffleQueue(
                    returnMedias(),
                    true
                )
            }

            textColor(R.color.md_black_1000)
            backgroundColor(R.color.md_blue_500)
            strokeColor(R.color.md_blue_500)
            iconColor(R.color.md_black_1000)
            rippleColor(R.color.md_blue_grey_200)
        }
    }

    abstract fun returnMedias(): List<Media>

    private fun setAppBarLayout(){
        val toolbar = binding.appBarLayout.toolbar

        mainActivity.setSupportActionBar(toolbar)

        toolbar.backgroundColor(R.color.transparent)
        binding.appBarLayout.setTextColor(R.color.md_white_1000)

        mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        binding.appBarLayout.setExpanded(true, true)
    }

    private fun setUpRecyclerView() {
        if(createAdapter() != null){
            adapterFolder = createAdapter()

            binding.fragmentContent.insetsConstraintLayout.apply {
                layoutAnimation = AnimationUtils
                    .loadLayoutAnimation(
                        requireContext(),
                        R.anim.item_appearance
                    )
            }

            binding.fragmentContent.recyclerView.apply {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = adapterFolder

                createFastScroller(this)
            }

            adapterFolder?.registerAdapterDataObserver(
                object : RecyclerView.AdapterDataObserver() {
                    override fun onChanged() {
                        super.onChanged()



                        checkForPadding()
                    }
                }
            )
        }

        checkForPadding()
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

    private fun checkForPadding() {
        binding.fragmentContent.recyclerView.updatePadding(
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

    fun setText(@StringRes stringId: Int){
        binding.fragmentContent.songTitle.text = resources.getString(stringId)
    }

    protected abstract fun createAdapter(): A?

    fun setToolbarTitle(string: String){
        binding.appBarLayout.collapsingTitle = string
        binding.appBarLayout.collapsingToolbar?.setCollapsedTitleTextAppearance(
            R.style.ToolbarTextAppearanceNormal
        )
    }



    override fun setUpGridSizeMenu(
        gridSizeMenu: SubMenu
    ) {
        return
    }

    override fun onSetSelectedItem(
        item: MenuItem
    ): Boolean {
        if(handleSortOrderMenuItem(item)){
            return true
        }

        if(selectedAddPlaylist(item)){
            return true
        }

        return false
    }

    abstract fun selectedAddPlaylist(item: MenuItem): Boolean

    override fun handleGridSizeMenuItem(
        item: MenuItem
    ): Boolean {
        return true
    }

    override fun loadGridSize(): Int {
        return 0
    }

    override fun setAndSaveGridSize(gridSize: Int) {
        return
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}