package com.ps.realize.ui.cropImage;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.isseiaoki.simplecropview.CropImageView;
import com.isseiaoki.simplecropview.callback.CropCallback;
import com.isseiaoki.simplecropview.callback.LoadCallback;
import com.isseiaoki.simplecropview.callback.SaveCallback;
import com.ps.realize.MainActivity;
import com.ps.realize.databinding.FragmentCropImageBinding;
import com.ps.realize.utils.AppListenerUtils;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;

// DATA_PASS_CONSTANT_1 pass value of  image that is to be cropped
public class CropImageFragment extends Fragment {

    private final String TAG = CropImageFragment.class.getSimpleName();
    FragmentCropImageBinding binding;

    private Fragment _this;
    private ImageButton cropViewRotateBtn;
    private ImageButton cropViewOkBtn;
    private ImageView backBtn;
    private CropImageView cropImageView;
    private String initialImageUriString;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _this = this;
        initialImageUriString = getArguments().getString(Constants.DATA_PASS_CONSTANT_1);

        binding = FragmentCropImageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();

//        if (savedInstanceState != null) {
//            // Restore last state for target video.
//            String videoUriString = savedInstanceState.getString(Constants.TARGET_VIDEO_URI, null);
//            if (videoUriString != null) {
//                playVideo(Uri.parse(videoUriString));
//            }
//        } else if (targetVideoURIString != null) {
//            playVideo(Uri.parse(targetVideoURIString));
//        }
        return root;
    }

    private void setViews() {
        cropViewRotateBtn = binding.createAddImgRotateBtn;
        cropViewOkBtn = binding.createAddImgOkBtn;
        backBtn = binding.cifIvBackBtn;
        cropImageView = binding.cropView;

        cropImageView.setCropMode(CropImageView.CropMode.FREE);

        cropImageView
                .load(Uri.parse(initialImageUriString))
                .useThumbnail(true)
                .execute(new LoadCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "successfully loaded image");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "Image load error", e);
                    }
                });


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentUtils.popFragmentsTillName(MainActivity.getMainActivity(), CropImageFragment.class.getSimpleName());
            }
        });
        SaveCallback cropSaveCallback = new SaveCallback() {
            @Override
            public void onSuccess(Uri uri) {
                AppListenerUtils.popBackDataHelper.passDataBackToListeners(String.valueOf(uri));
                FragmentUtils.popFragmentsTillName(MainActivity.getMainActivity(), CropImageFragment.class.getSimpleName());
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Error saving cropped image", e);
            }
        };

        cropViewRotateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImageView.rotateImage(CropImageView.RotateDegrees.ROTATE_90D);
            }
        });


        cropViewOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cropImageView.crop(Uri.parse(initialImageUriString))
                        .execute(new CropCallback() {
                            @Override
                            public void onSuccess(Bitmap cropped) {
                                cropImageView.save(cropped)
                                        .execute(Uri.parse(initialImageUriString), cropSaveCallback);
                            }

                            @Override
                            public void onError(Throwable e) {
                            }
                        });
            }
        });
    }

}
