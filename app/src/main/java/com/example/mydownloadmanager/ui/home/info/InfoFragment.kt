package com.example.mydownloadmanager.ui.home.info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.mydownloadmanager.MainActivity
import com.example.mydownloadmanager.R
import com.example.mydownloadmanager.common.lazyFast
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home_info.*

class InfoFragment : Fragment(R.layout.fragment_home_info) {
    private var player: SimpleExoPlayer? = null
    private lateinit var title: String
    private lateinit var fileName: String
    private lateinit var description: String
    private var playWhenReady = true
    private var currentWindow = 0
    private var playbackPosition: Long = 0
    private val filterDM by lazyFast { requireActivity().cacheDir.absolutePath }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       // (requireActivity() as MainActivity).nav_view.isVisible = false
        title = requireArguments().getString("key_title", "")
        fileName = requireArguments().getString("key_fileName", "")
        description = requireArguments().getString("key_description", "")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupView()
    }

    private fun setupView() {
        tv_title_name.text = title
        tv_description_info.text = description
    }

    private fun initializePlayer() {
        player = SimpleExoPlayer.Builder(requireContext()).build()
        video_view.player = player
        val mediaItem: MediaItem = MediaItem.fromUri("$filterDM/$fileName.mp4")
        player!!.setMediaItem(mediaItem)
        player!!.playWhenReady = playWhenReady
        player!!.seekTo(currentWindow, playbackPosition)
        player!!.prepare()
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT < 24 || player == null) {
            initializePlayer()
        }
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        video_view.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT < 24) {
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT >= 24) {
            releasePlayer()
        }
    }

    private fun releasePlayer() {
        if (player != null) {
            playWhenReady = player!!.playWhenReady
            playbackPosition = player!!.currentPosition
            currentWindow = player!!.currentWindowIndex
            player!!.release()
            player = null
        }
    }

    override fun onDestroyView() {
       // (requireActivity() as MainActivity).nav_view.isVisible = true
        super.onDestroyView()
    }
}