package com.frank.androidmedia.controller

import android.media.MediaPlayer
import android.media.PlaybackParams
import android.view.Surface
import com.frank.androidmedia.listener.PlayerCallback
import java.io.IOException

/**
 * The controller of MediaPlayer
 *
 * @author frank
 * @date 2022/3/18
 */
open class MediaPlayController(playerCallback: PlayerCallback) {

    private var mediaPlayer: MediaPlayer? = null
    private var renderFirstFrame: Boolean = false
    private var playerCallback: PlayerCallback? = null

    init {
        this.playerCallback = playerCallback
    }

    fun initPlayer(filePath: String, surface: Surface) {
        if (mediaPlayer != null) {
            releasePlayer()
        }
        try {
            renderFirstFrame = false
            mediaPlayer = MediaPlayer()
            mediaPlayer!!.setOnPreparedListener {
                mediaPlayer!!.start()
                playerCallback?.onPrepare()
            }
            mediaPlayer!!.setOnErrorListener { mp: MediaPlayer?, what: Int, extra: Int ->
                return@setOnErrorListener playerCallback?.onError(what, extra)!!
            }
            mediaPlayer!!.setOnCompletionListener {
                playerCallback?.onCompleteListener()
            }
            mediaPlayer!!.setOnInfoListener { mp, what, extra ->
                (
                        if (what == MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START) {
                            if (!renderFirstFrame) {
                                renderFirstFrame = true
                                playerCallback?.onRenderFirstFrame()
                            }
                        })
                return@setOnInfoListener true
            }
            mediaPlayer!!.setDataSource(filePath)
            mediaPlayer!!.setSurface(surface)
            mediaPlayer!!.prepareAsync()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun currentPosition(): Int {
        if (mediaPlayer == null)
            return 0
        return mediaPlayer!!.currentPosition
    }

    fun duration(): Int {
        if (mediaPlayer == null)
            return 0
        return mediaPlayer!!.duration
    }

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun togglePlay() {
        if (mediaPlayer == null)
            return

        if (mediaPlayer!!.isPlaying) {
            mediaPlayer!!.pause()
        } else {
            mediaPlayer!!.start()
        }
    }

    fun getVideoWidth(): Int {
        return mediaPlayer!!.videoWidth
    }

    fun getVideoHeight(): Int {
        return mediaPlayer!!.videoHeight
    }

    fun mute() {
        mediaPlayer?.setVolume(0.0f, 0.0f)
    }

    fun setVolume(volume: Float) {
        if (volume < 0 || volume > 1)
            return
        mediaPlayer?.setVolume(volume, volume)
    }

    /**
     * Set playback rate
     */
    fun setSpeed(speed: Float) {
        if (speed <= 0 || speed > 8)
            return
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            val params = PlaybackParams()
            params.speed = speed
            mediaPlayer?.playbackParams = params
        }
    }

    /**
     * Select audio or subtitle track, when there are multi tracks
     */
    fun selectTrack(trackId: Int) {
        mediaPlayer?.selectTrack(trackId)
    }

    fun releasePlayer() {
        if (mediaPlayer != null) {
            mediaPlayer!!.stop()
            mediaPlayer!!.release()
            mediaPlayer = null
        }
    }

}