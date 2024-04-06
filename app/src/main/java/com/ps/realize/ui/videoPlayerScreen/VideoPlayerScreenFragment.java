package com.ps.realize.ui.videoPlayerScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.ps.realize.MainActivity;
import com.ps.realize.R;
import com.ps.realize.core.datamodels.internal.VideoItem;
import com.ps.realize.databinding.FragmentVideoPlayerScreenBinding;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.JSONUtils;

public class VideoPlayerScreenFragment extends Fragment {


    private String videoItemDataStr;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        FragmentVideoPlayerScreenBinding binding = FragmentVideoPlayerScreenBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setViews(view);
    }

    private void setViews(View view) {
        Bundle args = getArguments();
        videoItemDataStr = args.getString(Constants.DATA_PASS_CONSTANT_1);
        VideoItem videoItem = JSONUtils.getGsonParser().fromJson(videoItemDataStr, VideoItem.class);


        TextView videotitle = view.findViewById(R.id.fvps_video_title);
        String title = videoItem.getTitle();
        if (title.length() > 30) {
            title = title.substring(0, 30) + "...";
        }
        videotitle.setText("" + title);


        Button selectBtn = view.findViewById(R.id.fvps_select_btn);
        selectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO
            }
        });

        YouTubePlayerView youTubePlayerView = view.findViewById(R.id.fvps_player_view);
        MainActivity.getMainActivity().getLifecycle().addObserver(youTubePlayerView);
        youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> {
            youTubePlayer.loadVideo(videoItem.getId(), 0);
        });

    }


}
