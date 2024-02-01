package com.example.elect.mediaplayer.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.elect.mediaplayer.R
import com.example.elect.mediaplayer.adapter.music.MusicTabPagerAdapter
import com.example.elect.mediaplayer.databinding.FragmentMusicBinding
import com.example.elect.mediaplayer.extensions.backgroundColor
import com.example.elect.mediaplayer.extensions.rippleColor
import com.example.elect.mediaplayer.extensions.selectedTabIndicatorColor
import com.example.elect.mediaplayer.extensions.tabTextColor
import com.example.elect.mediaplayer.interfaces.IBottomNavSelectedHelper
import com.example.elect.mediaplayer.util.PreferenceUtil
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.abs

abstract class BaseTabPagerFragment (
    @LayoutRes layout: Int
) : BaseActivityFragment(layout),
    IBottomNavSelectedHelper {

    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2
    private lateinit var musicTabPagerAdapter: MusicTabPagerAdapter
    private lateinit var _binding: FragmentMusicBinding
    private val binding get() = _binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMusicBinding
            .inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val toolbar = binding.appBarLayout.toolbar

        mainActivity.setSupportActionBar(toolbar)
        toolbar.backgroundColor(R.color.md_blue_grey_900)
        binding.appBarLayout.setTextColor(R.color.md_white_1000)
        mainActivity.supportActionBar?.setDisplayShowTitleEnabled(false)
        val string = resources.getString(R.string.music)
        binding.appBarLayout.title = string
        toolbar.setTitleTextAppearance(
            context, R.style.ToolbarTextAppearanceNormal
        )

        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager2

        musicTabPagerAdapter = MusicTabPagerAdapter(binding.appBarLayout, mainActivity)

        viewPager2.adapter = musicTabPagerAdapter
        viewPager2.setCurrentItem(PreferenceUtil.viewPagerPosition, false)

        viewPager2.registerOnPageChangeCallback(
            object : ViewPager2.OnPageChangeCallback() {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels)
                }

                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                }
            }
        )

        tabLayout.addOnTabSelectedListener(
            object : TabLayout.OnTabSelectedListener{
                override fun onTabSelected(tab: TabLayout.Tab) {
                    PreferenceUtil.viewPagerPosition = tab.position
                    viewPager2.currentItem = tab.position
                }
                override fun onTabUnselected(tab: TabLayout.Tab) { }
                override fun onTabReselected(tab: TabLayout.Tab?) { }
            }
        )

        TabLayoutMediator(
            tabLayout,
            viewPager2
        ){tab, position ->

            viewPager2.setCurrentItem(
                PreferenceUtil.viewPagerPosition,
                true
            )

            val tabName = when (position) {
                0 -> getString(R.string.tab_song)
                1 -> getString(R.string.tab_song_folder)
                2 -> getString(R.string.tab_album)
                3 -> getString(R.string.tab_artist)
                else -> ""
            }

            tab.text = tabName
        }.attach()

        viewPager2.setPageTransformer {page, position ->
            page.also {
                if(abs(position) >= 1f){
                    it.alpha = 0f
                    return@setPageTransformer
                }
                val scale = (1 - abs(position) / 2).coerceAtLeast(MIN_SCALE)
                it.scaleX = scale
                it.scaleY = scale
                it.alpha = (1 - abs(position)).coerceAtLeast(MIN_ALPHA)
                it.translationX = (1 - scale) * it.width / 2 * if(position > 0) -1 else 1
            }
        }

        setBackgroundColor()
    }

    private fun setBackgroundColor(){
        binding.fragmentRVLayout.backgroundColor(R.color.md_grey_900)
        binding.tabLayout.apply {
            backgroundColor(R.color.md_blue_grey_900)
            selectedTabIndicatorColor(R.color.md_blue_grey_400)
            tabTextColor(R.color.md_white_1000,R.color.md_blue_grey_400)
            rippleColor(R.color.md_blue_grey_400)
        }
    }

    override fun scrollToTop() {
        musicTabPagerAdapter.bottomNavSelected(PreferenceUtil.viewPagerPosition)
    }
}

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f