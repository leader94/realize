package com.ps.realize.core.helperClasses;

import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.interfaces.IArFragmentListener;

import java.util.ArrayList;
import java.util.List;

public class ArFragmentHelper {
    private static final String TAG = ArFragmentHelper.class.getSimpleName();
    private final List<IArFragmentListener> listeners = new ArrayList<>();

    public static void loadMediaPlayerVideosOfMapDetails(MapDetails mapDetail, Activity activity) {
        String videoUrl = null;
        mapDetail.mediaPlayer = new MediaPlayer();
        try {
            videoUrl = mapDetail.videos.get(0).getLocalUrl();
            mapDetail.mediaPlayer.setDataSource(MyApp.getContext(), Uri.parse(videoUrl));
            mapDetail.mediaPlayer.prepareAsync();
            Log.i(TAG, "MMM: Preparing localurl " + videoUrl);
        } catch (Exception e) {
            Log.e(TAG, "cannot play localurl " + videoUrl, e);
            videoUrl = mapDetail.videos.get(0).getUrl();
            try {
                mapDetail.mediaPlayer.setDataSource(activity.getApplicationContext(), Uri.parse(videoUrl));
                mapDetail.mediaPlayer.prepareAsync();
                Log.i(TAG, "MMM: Preparing remoteUrl " + videoUrl);
            } catch (Exception e1) {
                Log.e(TAG, "cannot play remote url " + videoUrl, e);
                e1.printStackTrace();
                // TODO add visual cue to user if getUrl also fails
            }
        }

        mapDetail.mediaPlayer.setLooping(true);


        mapDetail.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
//                mapDetail.mediaPlayer.start();
                Log.i(TAG, "VIdeoReady");
                mapDetail.mediaPlayerReady = true;
            }

        });
    }

    public static void loadExoPlayerVideosOfMapDetails(MapDetails mapDetail) {
        final String[] videoUrl = {null, null};
        mapDetail.exoPlayer = new SimpleExoPlayer.Builder(MyApp.getContext()).build();
        try {
            videoUrl[0] = mapDetail.videos.get(0).getLocalUrl();
            videoUrl[1] = mapDetail.videos.get(0).getUrl();

            Log.i(TAG, "AM: Preparing localurl " + videoUrl[0]);
            mapDetail.exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl[0])));
            mapDetail.exoPlayer.prepare();

            // Set up MediaSource event listener
            mapDetail.exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
                    Player.Listener.super.onPlayerError(error);
                    Log.e(TAG, "AM: Load error: " + error.getMessage());

                    if (mapDetail.playingFallbackUrl) {
                        Log.e(TAG, "AM: Failed to play any video " + error.getMessage());
                        mapDetail.exoPlayer.release();
                        return;
                    }

                    if (mapDetail.playingRemoteUrl) {
                        mapDetail.playingFallbackUrl = true;
                        Log.i(TAG, "AM: Preparing fallbackUrl " + videoUrl[1]);
                        // Fallback to video not found
                        int rawResourceId = R.raw.video_not_found;
                        Uri fallbackVideoUri = Uri.parse("android.resource://" + MyApp.getContext().getPackageName() + "/" + rawResourceId);
                        mapDetail.exoPlayer.setMediaItem(MediaItem.fromUri(fallbackVideoUri));
                        mapDetail.exoPlayer.prepare();
                        return;
                    }

                    try {
                        Log.i(TAG, "AM: Preparing remoteUrl " + videoUrl[1]);
                        mapDetail.playingRemoteUrl = true;
                        mapDetail.exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl[1])));
                        mapDetail.exoPlayer.prepare();

                    } catch (Exception e1) {
                        Log.e(TAG, "AM: cannot play remote url " + videoUrl[0], e1);
                        e1.printStackTrace();

                    }
                }


            });

        } catch (Exception e) {
            Log.e(TAG, "AM: cannot play localurl " + videoUrl[0], e);
            videoUrl[0] = mapDetail.videos.get(0).getUrl();
            try {
                mapDetail.exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl[0])));
                mapDetail.exoPlayer.prepare();
                Log.i(TAG, "AM: Preparing remoteUrl " + videoUrl[0]);
            } catch (Exception e1) {
                Log.e(TAG, "AM: cannot play remote url " + videoUrl[0], e);
                e1.printStackTrace();

            }
        }

        // Video is loaded and ready to play
        // Video is not ready
        mapDetail.mediaPlayerReady = mapDetail.exoPlayer.getDuration() > 0;

        mapDetail.exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_READY) {
                    mapDetail.exoPlayer.pause();
                    // Player is ready to play
                    mapDetail.mediaPlayerReady = true;
                }

                if (playbackState == Player.STATE_ENDED) {
                    // When playback reaches the end, seek to the beginning and play again
                    mapDetail.exoPlayer.seekTo(0);
                    mapDetail.exoPlayer.play();
                }
            }
        });


    }

    public void addListener(IArFragmentListener listener) {
        listeners.add(listener);
    }

    public void removeListener(IArFragmentListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners() {
        for (IArFragmentListener listener : listeners) {
            listener.onFragmentShown();
        }
    }


}
