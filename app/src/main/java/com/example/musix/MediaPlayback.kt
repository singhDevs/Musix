package com.example.musix

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import com.example.musix.callbacks.MediaControllerCallback
import com.example.musix.services.MusicService.Companion.NOT_REPEATED
import com.example.musix.services.MusicService.Companion.NOT_STARTED
import com.example.musix.services.MusicService.Companion.NO_SHUFFLE

object MediaPlayback{
    var mediaControllerCallback: MediaControllerCallback? = null
    var mediaController: MediaController? = null
    var mediaSession: MediaSession? = null
    var player: ExoPlayer? = null
    var musicStatus: Int = NOT_STARTED
    var repeatStatus: Int = NOT_REPEATED
    var shuffleStatus: Int = NO_SHUFFLE
}