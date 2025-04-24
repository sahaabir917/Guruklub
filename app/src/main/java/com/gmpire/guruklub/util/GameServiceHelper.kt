package com.gmpire.guruklub.util

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import com.gmpire.guruklub.R

object GameServiceHelper {

    var isMusicRunning = false

    fun playMusic(context: Context) {
        isMusicRunning = true
        context.startService(Intent(context, GameService::class.java))
    }

    fun stopMusic(context: Context) {
        isMusicRunning = false
        context.stopService(Intent(context, GameService::class.java))
    }

}