package com.gmpire.guruklub.util

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder
import com.gmpire.guruklub.R

class GameService : Service() {
    private lateinit var backgroundMusic: MediaPlayer
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        backgroundMusic = MediaPlayer.create(this, R.raw.backgorund_music)
        backgroundMusic.isLooping = true
        backgroundMusic.start()
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (this::backgroundMusic.isInitialized) {
            backgroundMusic.stop()
            backgroundMusic.release()
        }
    }
}