package com.ps.realize.ui.youtubeSearch;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.ps.realize.MainActivity;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.datamodels.internal.VideoItem;
import com.ps.realize.ui.videoPlayerScreen.VideoPlayerScreenFragment;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.JSONUtils;

import java.util.List;

public class VideoThumbnailAdapter extends RecyclerView.Adapter<VideoThumbnailAdapter.ViewHolder> {

    private final List<VideoItem> videoList;
    boolean showYoutubePlayer = false;
    boolean showTitle = true;
    boolean showImageThumbnail = true;

    public VideoThumbnailAdapter(List<VideoItem> videoList) {
        this.videoList = videoList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.video_list_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        VideoItem videoItem = videoList.get(position);

        if (showTitle) {
            String title = videoItem.getTitle();
            if (title.length() > 30) {
                title = title.substring(0, 30) + "...";
            }

            holder.title.setText("" + title);
        }

        if (showImageThumbnail) {
            Glide.with(MyApp.getContext()).load(videoItem.thumbnails).into(holder.imageView);

//            holder.imageView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//
//                }
//            });
        }

        if (showYoutubePlayer) {
            MainActivity.getMainActivity().getLifecycle().addObserver(holder.youTubePlayerView);
            holder.youTubePlayerView.getYouTubePlayerWhenReady(youTubePlayer -> {
                youTubePlayer.cueVideo(videoItem.getId(), 0);
            });
        }


        holder.ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String videoItemStr = JSONUtils.getGsonParser().toJson(videoItem);
                Bundle args = new Bundle();
                args.putString(Constants.DATA_PASS_CONSTANT_1, videoItemStr);
                VideoPlayerScreenFragment videoPlayerScreenFragment = new VideoPlayerScreenFragment();
                videoPlayerScreenFragment.setArguments(args);

                FragmentUtils.replaceFragment((AppCompatActivity) MainActivity.getMainActivity(), R.id.main_fragment_holder, videoPlayerScreenFragment, VideoPlayerScreenFragment.class.getSimpleName());
            }
        });


    }

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        YouTubePlayerView youTubePlayerView;

        ImageView imageView;
        TextView title;

        LinearLayout ll;

        ViewHolder(View itemView) {
            super(itemView);
            ll = itemView.findViewById(R.id.video_list_ll);
            title = itemView.findViewById(R.id.youtube_video_title);
            imageView = itemView.findViewById(R.id.thumbnail_image);


//            if (showYoutubePlayer) {
//                youTubePlayerView = itemView.findViewById(R.id.youtube_player_view);
//            }


//            // Get the display metrics
//            DisplayMetrics displayMetrics = new DisplayMetrics();
//            MainActivity.getMainActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
//
//            // Calculate the desired width (50% of the device width)
//            int width = displayMetrics.widthPixels / 2; // 50% of device width
//            youTubePlayerView.getLayoutParams().width = width;

        }


    }


}
