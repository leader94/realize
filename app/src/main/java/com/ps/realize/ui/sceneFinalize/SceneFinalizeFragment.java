package com.ps.realize.ui.sceneFinalize;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.databinding.FragmentSceneFinalizeBinding;
import com.ps.realize.ui.upload.UploadFragment;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.KeyboardUtils;

public class SceneFinalizeFragment extends Fragment {
    private static final String TAG = SceneFinalizeFragment.class.getSimpleName();
    FragmentSceneFinalizeBinding binding;
    Fragment _this;
    private SimpleExoPlayer exoPlayer;
    private String targetVideoURIString, targetImageURIString;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        _this = this;
        try {
            targetImageURIString = getArguments().getString(Constants.TARGET_IMAGE_URI);
            targetVideoURIString = getArguments().getString(Constants.TARGET_VIDEO_URI);

        } catch (Exception e) {
            Log.e(TAG, "unable to get target image or video URI", e);
        }

        binding = FragmentSceneFinalizeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();
        return root;

    }

    private void setViews() {
        ImageView targetImageView = binding.sffTagetImageView;
        ImageView backBtn = binding.sffBackBtn;
        TextView saveBtn = binding.sffNextBtn;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.backPress(_this);
            }
        });
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UploadFragment frag = new UploadFragment();
                Bundle args = new Bundle();
                args.putString(Constants.TARGET_IMAGE_URI, targetImageURIString);
                args.putString(Constants.TARGET_VIDEO_URI, targetVideoURIString);
                frag.setArguments(args);
                FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                        R.id.main_fragment_holder,
                        frag,
                        frag.getClass().getSimpleName()
                );
            }
        });


        Glide.with(_this)
                .load(targetImageURIString)
                .into(targetImageView);


        addExoVideoPlayer();

    }

    /**
     *
     */
    @Override
    public void onDestroyView() {
        if (exoPlayer != null) {
            exoPlayer.release();
        }
        super.onDestroyView();
    }

    /**
     *
     */
    @Override
    public void onPause() {
        super.onPause();
        if (exoPlayer != null) {
            exoPlayer.pause();
        }
    }

    /**
     *
     */
    @Override
    public void onResume() {
        super.onResume();
        if (exoPlayer != null) {
            exoPlayer.play();
        }
    }

    private void addExoVideoPlayer() {
        try {
            SurfaceView surfaceView = binding.sffTargetVideoView;
            exoPlayer = new SimpleExoPlayer.Builder(MyApp.getContext()).build();

            exoPlayer.setVideoSurfaceView(surfaceView);
            exoPlayer.setMediaItem(MediaItem.fromUri(Uri.parse(targetVideoURIString)));
            exoPlayer.prepare();

            exoPlayer.addListener(new Player.Listener() {
                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_READY) {
                        exoPlayer.play();
                    }

                    if (playbackState == Player.STATE_ENDED) {
                        // When playback reaches the end, seek to the beginning and play again
                        exoPlayer.seekTo(0);
                        exoPlayer.play();
                    }
                }
            });
        } catch (Exception e) {

        }
    }
}

