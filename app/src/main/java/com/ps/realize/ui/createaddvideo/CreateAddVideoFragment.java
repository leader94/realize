package com.ps.realize.ui.createaddvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.ps.realize.databinding.FragmentCreateAddVideoBinding;
import com.ps.realize.utils.CommonUtils;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.MediaUtils;

import java.io.File;
import java.io.IOException;

public class CreateAddVideoFragment extends Fragment {
    private final String TAG = CreateAddVideoFragment.class.getSimpleName();
    private final CommonUtils commonUtils = new CommonUtils();
    ActivityResultLauncher<Uri> videoFromCameraActivity;
    ActivityResultLauncher<Intent> videoFromLocalStorageActivity;
    private FragmentCreateAddVideoBinding binding;
    private Fragment _this;
    private ImageView backBtn;
    private EditText etUrl;

    private VideoView videoView;
    private MediaController mediaController;
    private Uri cameraVideoUri;
    private LinearLayout llCenter, urlPopUp;
    private RelativeLayout videoContainer;
    private ProgressBar progressBar;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;
        attachActivityResultLaunchers();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        binding = FragmentCreateAddVideoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setViews() {
        LinearLayout llCameraVideo = binding.createAddVideoCamera;
        LinearLayout llLocalVideo = binding.createAddVideoLocal;
        LinearLayout llUrl = binding.createAddVideoUrl;
        llCenter = binding.createAddVideoCenterLl;
        urlPopUp = binding.createAddVideoUrlPopup;
        etUrl = binding.createAddVideoUrlEdittext;
        videoView = binding.createAddVideoTargetVideoView;
        backBtn = binding.createAddVideoBackBtn;
        videoContainer = binding.createAddVideoVideoContainer;
        progressBar = binding.createAddVideoIndeterminateBar;

        mediaController = new MediaController(getContext());
        mediaController.setAnchorView(videoView);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.backPress(_this);
            }
        });

        llCameraVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                videoFromCameraActivity.launch(getTempCameraVideoUri());
            }
        });

        llLocalVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaUtils.pickVideoFromGalleryIntent(videoFromLocalStorageActivity);
            }
        });

        llUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                urlPopUp.setVisibility(View.VISIBLE);
            }
        });

        etUrl.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    String text = String.valueOf(etUrl.getText());
                    urlPopUp.setVisibility(View.GONE);
                    playVideo(text);
                    return false;  // Intentional to let the soft keyboard close
                }
                return false;
            }
        });
    }

    private void attachActivityResultLaunchers() {
        videoFromCameraActivity = registerForActivityResult(new ActivityResultContracts.CaptureVideo(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean success) {
                if (success) {
                    playVideo(cameraVideoUri);
                } else {
                    Log.e(TAG, "Error retriving video from camera activity");
                    videoContainer.setVisibility(View.GONE);
                    llCenter.setVisibility(View.VISIBLE);
                }
            }
        });


        videoFromLocalStorageActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data == null) return;

                            try {
                                final Uri videoUri = data.getData();
                                playVideo(videoUri);
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    }
                });
    }

    private void playVideo(Uri uri) {
        playVideoPreSetup();
        videoView.setVideoURI(uri);
//        videoView.requestFocus();   // commented to allow popup hide softkeyboard
    }

    private void playVideo(String path) {
        playVideoPreSetup();
        videoView.setVideoPath(path);
    }

    private void playVideoPreSetup() {
        progressBar.setVisibility(View.VISIBLE);
        videoContainer.setVisibility(View.VISIBLE);
        llCenter.setVisibility(View.GONE);
        videoView.setMediaController(null);
        videoView.setKeepScreenOn(true);

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }

            }
        });


        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                progressBar.setVisibility(View.GONE);
                videoView.start();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                progressBar.setVisibility(View.GONE);
                return false;
            }
        });
    }

    private Uri getTempCameraVideoUri() {
        File imagePath = null;
        try {
            imagePath = File.createTempFile(
                    "IMG_",
                    ".mp4",
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cameraVideoUri = commonUtils.getURI(imagePath, _this);
        return cameraVideoUri;

    }
}
