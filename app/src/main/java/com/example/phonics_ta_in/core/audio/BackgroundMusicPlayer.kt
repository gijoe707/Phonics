package com.example.phonics_ta_in.core.audio

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.example.phonics_ta_in.R

/**
 * Looping background music for the main (learning path) page.
 *
 * It plays only while ALL of these are true:
 *  - the main page asks for it ([setWantedOnScreen]), and
 *  - the app is visible to the user ([setAppVisible]), so it never plays from the background, and
 *  - music is allowed ([setAllowed]): the parent setting is on and the child has not muted it.
 */
class BackgroundMusicPlayer(context: Context) {
    private val appContext = context.applicationContext
    private var player: MediaPlayer? = null
    private var wantedOnScreen = false
    private var appVisible = false
    private var allowed = true
    private var released = false

    fun setWantedOnScreen(wanted: Boolean) {
        wantedOnScreen = wanted
        update()
    }

    fun setAppVisible(visible: Boolean) {
        appVisible = visible
        update()
    }

    fun setAllowed(isAllowed: Boolean) {
        allowed = isAllowed
        update()
    }

    fun release() {
        released = true
        player?.release()
        player = null
    }

    private fun update() {
        if (released) return
        if (wantedOnScreen && appVisible && allowed) {
            val mp = player ?: create() ?: return
            if (!mp.isPlaying) mp.start()
        } else {
            player?.let { if (it.isPlaying) it.pause() }
        }
    }

    private fun create(): MediaPlayer? {
        val mp = try {
            MediaPlayer.create(appContext, R.raw.happy_commercial_piano)
        } catch (e: Exception) {
            Log.w(TAG, "Could not create background music player", e)
            null
        } ?: return null
        mp.isLooping = true
        mp.setVolume(VOLUME, VOLUME)
        player = mp
        return mp
    }

    private companion object {
        const val TAG = "BackgroundMusic"
        const val VOLUME = 0.35f // soft, so it never covers the spoken letters
    }
}
