package com.example.elect.mediaplayer.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.FrameLayout
import androidx.core.animation.doOnEnd
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.navigation.ui.setupWithNavController
import com.example.elect.mediaplayer.BottomSheetBehavior
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.activity.base.BaseMusicServiceActivity
import com.example.elect.mediaplayer.databinding.ActivityMainBinding
import com.example.elect.mediaplayer.extensions.*
import com.example.elect.mediaplayer.fragments.base.BasePlayerFragment
import com.example.elect.mediaplayer.fragments.base.BaseToolbarFragment
import com.example.elect.mediaplayer.fragments.favorite.FavoriteFragment
import com.example.elect.mediaplayer.fragments.music.MusicFragment
import com.example.elect.mediaplayer.fragments.other.MiniPlayerFragment
import com.example.elect.mediaplayer.fragments.player.SimplePlayerFragment
import com.example.elect.mediaplayer.fragments.playlist.PlaylistFragment
import com.example.elect.mediaplayer.fragments.video.VideoFolderFragment
import com.example.elect.mediaplayer.helper.PlayerRemote
import com.example.elect.mediaplayer.interfaces.IBottomNavSelectedHelper
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.example.elect.mediaplayer.views.InsetsCoordinatorLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetBehavior.*

class MainActivity : BaseMusicServiceActivity() {

    private lateinit var binding : ActivityMainBinding
    private lateinit var bottomNav : BottomNavigationView
    private lateinit var snackBar: InsetsCoordinatorLayout

    private var windowInsets: WindowInsetsCompat? = null
    private var simplePlayerFragment: BasePlayerFragment? = null

    private var miniPlayerFragment: MiniPlayerFragment? = null

    private val bottomSheetCallbackList =
        object : BottomSheetCallback() {

            override fun onSlide(
                bottomSheet: View,
                slideOffset: Float) {

                setMiniPlayerAlphaProgress(slideOffset)

            }

            override fun onStateChanged(
                bottomSheet: View,
                newState: Int) {
                when (newState) {
                    STATE_EXPANDED -> {
                        onPanelExpanded()
                        statusBarColor(R.color.md_black_1000)
                        navigationBarColor(R.color.md_grey_800)
                    }
                    STATE_COLLAPSED -> {
                        onPanelCollapsed()
                        statusBarColor(R.color.md_blue_grey_900)
                        navigationBarColor(R.color.md_blue_grey_900)
                    }
                    STATE_SETTLING, STATE_DRAGGING -> {
                        statusBarColor(R.color.md_blue_grey_900)
                        navigationBarColor(R.color.md_grey_800)
                    }
                    else -> {
                        println("Do a flip")
                        statusBarColor(R.color.md_blue_grey_900)
                        navigationBarColor(R.color.md_grey_800)
                    }
                }
            }
        }

    private lateinit var bottomSheetBehavior: BottomSheetBehavior<FrameLayout>

    fun setAllowDrag(isDraggable: Boolean){
        bottomSheetBehavior.setAllowDragging(isDraggable)
    }

    private val panelState: Int
        get() = bottomSheetBehavior.state

    fun getBottomSheetBehavior() = bottomSheetBehavior

    fun getSnackBar() = snackBar

    private fun setMiniPlayerAlphaProgress(progress: Float) {
        if (progress < 0) return
        val alpha = 1 - progress
        miniPlayerFragment?.view?.alpha = 1 - (progress / 0.2F)
        miniPlayerFragment?.view?.isGone = alpha == 0f

        binding.bottomNavigationView.translationY = progress * 500
        binding.bottomNavigationView.alpha = alpha

        binding.bottomNavigationView.apply {
            backgroundColor(R.color.md_blue_grey_900)
            itemRippleColor(R.color.md_blue_grey_400)
            activeItemColor(R.color.md_blue_grey_800)
        }

        binding.simplePlayerContainer.apply {
            this.alpha = (progress - 0.2F) / 0.2F
        }
    }

    private fun onPanelExpanded() {
        setMiniPlayerAlphaProgress(1F)

        simplePlayerFragment?.onShow()
    }

    private fun onPanelCollapsed() {
        setMiniPlayerAlphaProgress(0F)
        simplePlayerFragment?.onHide()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.slidingColorPanel.backgroundColor(R.color.md_blue_grey_900)

        statusBarColor(R.color.md_blue_grey_900)
        navigationBarColor(R.color.md_blue_grey_900)

        PreferenceUtil.appbarMode = 0

        setUpMiniPlayerFragment()

        bottomNav = binding.bottomNavigationView
        snackBar = binding.snackBarFrameLayout

        ViewCompat.setOnApplyWindowInsetsListener(
            binding.root
        ) { _, insets ->
            windowInsets = insets
            insets
        }

        setupBottomSheet()

        setupSlidingUpPanel()

        setUpNavigationController()
    }

    private fun setUpMiniPlayerFragment(){
        val emptyPlayer: Fragment = SimplePlayerFragment()

        supportFragmentManager.commit {
            replace(R.id.simplePlayerContainer, emptyPlayer)
        }

        supportFragmentManager.executePendingTransactions()

        simplePlayerFragment = whichFragment<BasePlayerFragment>(
            R.id.simplePlayerContainer
        )

        miniPlayerFragment = whichFragment<MiniPlayerFragment>(
            R.id.miniPlayerFragment
        )
        miniPlayerFragment?.view?.bringToFront()
        miniPlayerFragment?.view?.setOnClickListener {

            Intent(
                this,
                PlayerActivity::class.java
            ).apply {
                startActivity(this)
            }
        }
    }

    private fun setupSlidingUpPanel() {

        binding.slidingPanel.apply {
            viewTreeObserver
                .addOnGlobalLayoutListener(
                    object : ViewTreeObserver.OnGlobalLayoutListener {

                        override fun onGlobalLayout() {
                            binding.slidingPanel.viewTreeObserver
                                .removeOnGlobalLayoutListener(this)
                            when (panelState) {
                                STATE_EXPANDED -> onPanelExpanded()
                                STATE_COLLAPSED -> onPanelCollapsed()
                                else -> {

                                }
                            }
                        }
                    })
        }
    }

    private fun setupBottomSheet() {
        bottomSheetBehavior =
            from(binding.slidingPanel) as BottomSheetBehavior<FrameLayout>

        bottomSheetBehavior.addBottomSheetCallback(
            bottomSheetCallbackList)
        bottomSheetBehavior.isHideable = false
        setMiniPlayerAlphaProgress(0F)
    }

    fun collapsePanel() {
        bottomSheetBehavior.state = STATE_COLLAPSED
    }

    private fun expandPanel() {
        bottomSheetBehavior.state = STATE_EXPANDED
    }

    private fun handleBackPress(): Boolean {
        if (
            bottomSheetBehavior.peekHeight != 0 &&

            simplePlayerFragment!!.onBackPressed()
        ) return true

        if (panelState == STATE_EXPANDED) {
            collapsePanel()
            return true
        }
        return false
    }

    override fun onBackPressed() {

        if (!handleBackPress()) super.onBackPressed()
    }


    private fun setUpNavigationController(){

        val navController =
            findNavController(R.id.fragment_container)

        with(navController){

            val navInflater = this.navInflater
            val navGraph = navInflater
                .inflate(R.navigation.main_graph)

            navGraph.setStartDestination(
                getNumberOrLayoutId(
                    PreferenceUtil.lastTab,
                    getLayoutId = true
                )
            )

            graph = navGraph


            addOnDestinationChangedListener{_, destination, _ ->

                PreferenceUtil.lastTab =
                    getNumberOrLayoutId(
                        destination.id,
                        getNumber = true
                    )

                when(destination.id){
                    R.id.action_video,
                    R.id.action_music,
                    R.id.action_favorite,
                    R.id.action_playlist -> {
                        if(
                            PlayerRemote.isPlaying ||
                            PlayerRemote.playingQueue.isNotEmpty()
                        ){
                            setBottomAlpha(
                                bottomSheetVisible = true,
                                bottomNavVisible = true
                            )
                        } else if(
                            !PlayerRemote.isPlaying &&
                            PlayerRemote.playingQueue.isEmpty()
                        ){
                            setBottomAlpha(
                                bottomSheetVisible = false,
                                bottomNavVisible = true
                            )
                        } else {
                            setBottomAlpha(
                                bottomSheetVisible = true,
                                bottomNavVisible = true
                            )
                        }
                    }
                    else -> {
                        if(
                            PlayerRemote.isPlaying ||
                            PlayerRemote.playingQueue.isNotEmpty()
                        ){
                            setBottomAlpha(
                                bottomSheetVisible = true,
                                bottomNavVisible = false
                            )
                        } else if(
                            !PlayerRemote.isPlaying &&
                            PlayerRemote.playingQueue.isEmpty()
                        ){
                            setBottomAlpha(
                                bottomSheetVisible = false,
                                bottomNavVisible = false
                            )
                        } else {
                            setBottomAlpha(
                                bottomSheetVisible = true,
                                bottomNavVisible = false
                            )
                        }
                    }
                }
            }


        }


        bottomNav.apply {

            setupWithNavController(navController)

            setOnItemReselectedListener {
                if(it.itemId == R.id.action_music){
                    navController.navigate(R.id.action_music)
                }
                currentFragment(R.id.fragment_container).apply {
                    if(this is IBottomNavSelectedHelper){
                        scrollToTop()
                    }
                }
            }
        }
    }

    private fun getNumberOrLayoutId(
        layoutIdOrNumber: Int,
        getLayoutId: Boolean = false,
        getNumber: Boolean = false
    ): Int{
        if(getLayoutId != getNumber){
            if(getLayoutId){
                return when(
                    layoutIdOrNumber
                ){
                    Category.Videos.number -> Category.Videos.layoutId
                    Category.Musics.number -> Category.Musics.layoutId
                    Category.Favorites.number -> Category.Favorites.layoutId
                    Category.Playlists.number -> Category.Playlists.layoutId
                    else -> Category.Videos.layoutId
                }
            }

            if(getNumber){
                return when(layoutIdOrNumber){
                    Category.Videos.layoutId -> Category.Videos.number
                    Category.Musics.layoutId -> Category.Musics.number
                    Category.Favorites.layoutId -> Category.Favorites.number
                    Category.Playlists.layoutId -> Category.Playlists.number
                    else -> Category.Videos.number
                }
            }
        }

        return 0
    }

    private fun setBottomAlpha(
        bottomSheetVisible: Boolean,
        bottomNavVisible: Boolean
    ){

        val translationY =
            if (bottomNavVisible) 0F

            else dip(R.dimen.bottom_nav_height).toFloat() +
                    windowInsets.safeGetBottomInsets()

        binding.bottomNavigationView
            .translateYAnimate(translationY).doOnEnd {

                if (bottomNavVisible &&
                    bottomSheetBehavior.state != STATE_EXPANDED
                ) {

                    binding.bottomNavigationView.bringToFront()
                }
            }

        hideBottomSheet(bottomSheetVisible, bottomNavVisible)
    }

    private fun hideBottomSheet(
        bottomSheetVisible: Boolean,
        bottomNavVisible: Boolean
    ) {
        bottomSheetBehavior.setAllowDragging(true)

        val heightOfBar =

            windowInsets.safeGetBottomInsets() + dip(R.dimen.mini_player_height)

        val heightOfBarWithTabs = heightOfBar + dip(R.dimen.bottom_nav_height)

        if (bottomSheetVisible) {

            binding.slidingPanel.elevation = 0F
            binding.bottomNavigationView.elevation = 5F

            if(bottomNavVisible){
                bottomSheetBehavior.peekHeightAnimate(heightOfBarWithTabs).doOnEnd {
                    binding.slidingPanel.bringToFront()
                }

                getSnackBar().updateMargin(-136f)
            } else {

                bottomSheetBehavior.peekHeightAnimate(heightOfBar).doOnEnd {
                    binding.slidingPanel.bringToFront()
                }

                getSnackBar().updateMargin(42f)
            }
        } else {
            if(bottomNavVisible){

                bottomSheetBehavior.peekHeightAnimate(
                    -windowInsets.safeGetBottomInsets()
                ).doOnEnd {}

                bottomSheetBehavior.state = STATE_COLLAPSED

                getSnackBar().updateMargin(0f)
            } else {

                bottomSheetBehavior.peekHeightAnimate(
                    -windowInsets.safeGetBottomInsets()
                ).doOnEnd {}

                bottomSheetBehavior.state = STATE_COLLAPSED

                getSnackBar().updateMargin(189f)
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean =
        findNavController(R.id.fragment_container).navigateUp()


    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val expand =
            intent?.extra<Boolean>(EXPAND_PANEL)?.value
            ?: false
        if (expand) {
            binding.slidingPanel.bringToFront()

            expandPanel()

            intent?.removeExtra(EXPAND_PANEL)
        } else {

            intent?.removeExtra(EXPAND_PANEL)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)

        if(
            !PlayerRemote.isPlaying &&
            PlayerRemote.playingQueue.isEmpty()
        ){
            selectHideBottom()
        } else {
            selectShowBottom()
        }

        currentFragment(R.id.fragment_container).apply {
            if(this is BaseToolbarFragment){
                setOptionsMenu()
            }
        }
    }

    fun selectHideBottom() {
        currentFragment(R.id.fragment_container).apply {
            when(this) {
                is MusicFragment,
                is VideoFolderFragment,
                is FavoriteFragment,
                is PlaylistFragment -> {
                    setBottomAlpha(
                        bottomSheetVisible = false,
                        bottomNavVisible = true
                    )
                }

                else -> {
                    setBottomAlpha(
                        bottomSheetVisible = false,
                        bottomNavVisible = false
                    )
                }
            }
        }
    }

    fun selectShowBottom() {
        currentFragment(R.id.fragment_container).apply {
            when(this) {
                is MusicFragment,
                is VideoFolderFragment,
                is FavoriteFragment,
                is PlaylistFragment -> {
                    setBottomAlpha(
                        bottomSheetVisible = true,
                        bottomNavVisible = true
                    )
                }

                else -> {
                    setBottomAlpha(
                        bottomSheetVisible = true,
                        bottomNavVisible = false
                    )
                }
            }
        }
    }

    companion object {

        const val EXPAND_PANEL = "expand_panel"

        const val IS_FADE_ACTIVITY = true
    }
}

enum class Category(
    val number: Int,
    val layoutId: Int
){
    Videos(0, R.id.action_video),
    Musics(1, R.id.action_music),
    Favorites(2 , R.id.action_favorite),
    Playlists(3, R.id.action_playlist)
}