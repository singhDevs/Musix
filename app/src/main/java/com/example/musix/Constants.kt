package com.example.musix

import androidx.media3.session.MediaController
import com.example.musix.callbacks.MediaControllerCallback
import com.example.musix.services.MusicService.Companion.NOT_REPEATED
import com.example.musix.services.MusicService.Companion.NOT_STARTED
import com.example.musix.services.MusicService.Companion.NO_SHUFFLE

object Constants{
    var mediaControllerCallback: MediaControllerCallback? = null
    var mediaController: MediaController? = null
    var musicStatus: Int = NOT_STARTED
    var repeatStatus: Int = NOT_REPEATED
    var shuffleStatus: Int = NO_SHUFFLE
}