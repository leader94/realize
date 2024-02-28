package com.ps.realize.core.helperClasses;

import android.media.MediaPlayer;
import android.view.Surface;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.ps.realize.core.datamodels.ar.VideoObj;

import java.util.List;


public class MapDetails {

    private final boolean useExo = true;
    public boolean playingRemoteUrl = false;
    public boolean playingFallbackUrl = false;
    public boolean tracked = false;
    public MediaPlayer mediaPlayer;
    public SimpleExoPlayer exoPlayer;
    //player.setMediaItem(MediaItem.fromUri(/* videoUri */));
//player.prepare();
    public boolean mediaPlayerReady = false;
    //    public  SimpleExoPlayer
    String type;
    List<VideoObj> videos;

    public MapDetails() {


    }

    public MapDetails(String type, List<VideoObj> videos) {
        this.type = type;
        this.videos = videos;
    }

    public void setMediaPlayerSurface(Surface surface) {
        if (useExo) {
            this.exoPlayer.setVideoSurface(surface);
        } else {
            this.mediaPlayer.setSurface(surface);

        }

    }

    public boolean isVideoPlaying() {
        if (useExo) {
            return this.exoPlayer.isPlaying();
        } else {
            return this.mediaPlayer.isPlaying();

        }
    }

    public void startVideoPlay() {
        if (useExo) {
            this.exoPlayer.play();
        } else {
            this.mediaPlayer.start();

        }
    }

    public void pauseVideoPlay() {
        if (useExo) {
            this.exoPlayer.pause();
        } else {
            this.mediaPlayer.pause();

        }
    }
}