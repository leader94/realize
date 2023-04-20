package com.ps.realize.ui.createaddimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ps.realize.R;
import com.ps.realize.core.interfaces.IOnBackPressed;
import com.ps.realize.databinding.FragmentCreateAddImageBinding;
import com.ps.realize.ui.createaddvideo.CreateAddVideoFragment;
import com.ps.realize.utils.CommonService;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.MediaUtils;

import java.io.File;
import java.io.IOException;

public class CreateAddImageFragment extends Fragment implements IOnBackPressed {
    private final String TAG = CreateAddImageFragment.class.getSimpleName();

    private final Constants constants = new Constants();
    private ActivityResultLauncher<Intent> imageFromLocalStorageActivity;
    private ActivityResultLauncher<Uri> imageFromCameraActivity;
    private Uri cameraPhotoUri;
    private ImageView ivTargetImage;
    private FragmentCreateAddImageBinding binding;
    private Fragment _this;
    private TextView tvNextBtn;
    private LinearLayout urlPopUp;
    private EditText etUrl;
    private LinearLayout createAddImageLL;
    private ImageView targetImageView;
    private ImageView backBtn;
    private RequestListener rlForTargetImage;
    private String targetImageURIString;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;
        attachActivityResultLaunchers();
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        CreateAddImageViewModel homeViewModel = new ViewModelProvider(this).get(CreateAddImageViewModel.class);

        binding = FragmentCreateAddImageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();
        setListeners();

        if (savedInstanceState != null) {
            // Restore last state for target image.
            String imageUriString = savedInstanceState.getString(constants.TARGET_IMAGE_URI, null);
            if (imageUriString != null) {
                setImageIntoImageView(Uri.parse(imageUriString));
            }
        } else if (targetImageURIString != null) {
            setImageIntoImageView(Uri.parse(targetImageURIString));
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(constants.TARGET_IMAGE_URI, targetImageURIString);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {
        rlForTargetImage = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                createAddImageLL.setVisibility(View.VISIBLE);
                targetImageView.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                createAddImageLL.setVisibility(View.GONE);
                targetImageView.setVisibility(View.VISIBLE);
                tvNextBtn.setVisibility(View.VISIBLE);
                return false;
            }
        };
    }

    private void setViews() {
        tvNextBtn = binding.createAddImgNextBtn;
        urlPopUp = binding.addImageLlUrlPopup;
        etUrl = binding.addImageEtUrl;
        ivTargetImage = binding.createAddImgTargetImage;
        createAddImageLL = binding.createAddImgLlAddBtn;
        targetImageView = binding.targetImageView;
//        create_add_img_back_btn;
        backBtn = binding.createAddImgBackBtn;


        LinearLayout llCameraImage = binding.createAddImgCamera;
        LinearLayout llLocalImage = binding.createAddImgLocal;
        LinearLayout llUrl = binding.createAddImgUrl;

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                KeyboardUtils.backPress(_this);
            }
        });
        tvNextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO store the path of image;
                CreateAddVideoFragment frag = new CreateAddVideoFragment();
                Bundle args = new Bundle();
                args.putString(constants.TARGET_IMAGE_URI, targetImageURIString);
                frag.setArguments(args);
                CommonService.replaceFragment((AppCompatActivity) getActivity(),
                        R.id.main_fragment_holder,
                        frag,
                        CreateAddVideoFragment.class.getSimpleName());

            }
        });
        llCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imageFromCameraActivity.launch(getTempCameraImageUri());
            }
        });
        llLocalImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MediaUtils.pickImageFromGalleryIntent(imageFromLocalStorageActivity);
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
                    setImageIntoImageView(Uri.parse(text));
                    return false;  // Intentional to let the soft keyboard close
                }
                return false;
            }
        });
    }

    private Uri getURI(File mediaFile) {
        return FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getPackageName() + ".provider", mediaFile);
    }

    private void attachActivityResultLaunchers() {
        imageFromCameraActivity = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean success) {
                if (success) {
                    setImageIntoImageView(cameraPhotoUri);
                } else {
                    Log.e(TAG, "Error retriving image from camera activity");
                }
            }
        });

        imageFromLocalStorageActivity = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            Intent data = result.getData();
                            if (data == null) return;

                            try {
                                final Uri imageUri = data.getData();
//                                final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
//                                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                                setImageIntoImageView(imageUri);
                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    }
                });
    }

    private void setImageIntoImageView(Uri imageUri) {
        targetImageView.setVisibility(View.VISIBLE);
        targetImageURIString = String.valueOf(imageUri);
        Glide.with(_this)
                .load(imageUri)
                .listener(rlForTargetImage)
                .into(targetImageView);
    }

    /*
    private void setImageIntoImageView(Bitmap selectedImage) {
        targetImageView.setVisibility(View.VISIBLE);
        Glide.with(_this)
                .load(selectedImage)
                .listener(rlForTargetImage)
                .into(targetImageView);
    }
    */

    private Uri getTempCameraImageUri() {
        File imagePath = null;
        try {
            imagePath = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        cameraPhotoUri = getURI(imagePath);
        return cameraPhotoUri;

    }

    @Override
    public boolean onBackPressed() {
        if (urlPopUp.getVisibility() == View.VISIBLE) {
            urlPopUp.setVisibility(View.GONE);
            return true;
        }
        return false;
    }
}