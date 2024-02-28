package com.ps.realize.ui.createaddvideo;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.FragmentCreateAddVideoBinding;
import com.ps.realize.ui.upload.UploadFragment;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.MediaUtils;
import com.ps.realize.utils.NetworkUtils;
import com.ps.realize.utils.SharedMediaUtils;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.Request;
import okhttp3.Response;

public class CreateAddVideoFragment extends Fragment {
    private final String TAG = CreateAddVideoFragment.class.getSimpleName();
    private final Constants constants = new Constants();
    private ActivityResultLauncher<Uri> videoFromCameraActivity;
    private ActivityResultLauncher<Intent> videoFromLocalStorageActivity;
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
    private String targetVideoURIString, targetImageURIString;

    private TextView nextBtn;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;
        attachActivityResultLaunchers();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        targetImageURIString = getArguments().getString(Constants.TARGET_IMAGE_URI);

        binding = FragmentCreateAddVideoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();

        if (savedInstanceState != null) {
            // Restore last state for target video.
            String videoUriString = savedInstanceState.getString(Constants.TARGET_VIDEO_URI, null);
            if (videoUriString != null) {
                playVideo(Uri.parse(videoUriString));
            }
        } else if (targetVideoURIString != null) {
            playVideo(Uri.parse(targetVideoURIString));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.TARGET_VIDEO_URI, targetVideoURIString);
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

        nextBtn = binding.createAddVideoNextBtn;

        mediaController = new MediaController(MyApp.getContext());
        mediaController.setAnchorView(videoView);

        nextBtn.setOnClickListener(new View.OnClickListener() {
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

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.backPress(_this);
            }
        });

        llCameraVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                videoFromCameraActivity.launch(getTempCameraVideoUri());
                cameraVideoUri = SharedMediaUtils.createVideoFile(getContext());
                videoFromCameraActivity.launch(cameraVideoUri);

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
                    downloadVideo(text); // todo move to async thread
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
                                MyApp.getContext().getContentResolver().takePersistableUriPermission(videoUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION);
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
        targetVideoURIString = String.valueOf(uri);
        videoView.setVideoURI(uri);
//        videoView.requestFocus();   // commented to allow popup hide softkeyboard
    }

    private void playVideo(String path) {
        playVideoPreSetup();
        targetVideoURIString = String.valueOf(path);

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
                nextBtn.setVisibility(View.VISIBLE);

            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
                progressBar.setVisibility(View.GONE);
                nextBtn.setVisibility(View.INVISIBLE);
                return false;
            }
        });
    }

    private void downloadVideo(String url) {
        NetworkUtils.get(url, null, new NetworkListener() {
            @Override
            public void onFailure(Request request, IOException e) {
            }

            @Override
            public void onResponse(Response response) {
                InputStream inputStream = response.body().byteStream();
                Uri localVideoUri = SharedMediaUtils.writeVideoFile(getContext(), inputStream);
                targetVideoURIString = String.valueOf(localVideoUri);
            }
        });

    }
    /*
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
    */
}
