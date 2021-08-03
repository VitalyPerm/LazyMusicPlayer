package com.elvitalya.lazymusicplayer.utils

import com.elvitalya.lazymusicplayer.presentation.MusicPlayerActivity
import java.util.concurrent.TimeUnit

lateinit var APP_ACTIVITY: MusicPlayerActivity


fun timerFormat(time: Long) : String {
    val result = String.format("%02d:%02d", TimeUnit.SECONDS.toMinutes(time),
        TimeUnit.SECONDS.toSeconds(time) - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(time)))
    var convert = ""
    for(i in result.indices){
        convert += result[i]
    }

    return convert
}





