package com.ps.realize.ui.createaddimage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.interfaces.CallbackListener;
import com.ps.realize.core.interfaces.IOnBackPressed;
import com.ps.realize.core.interfaces.IPopBackDataListener;
import com.ps.realize.databinding.FragmentCreateAddImageBinding;
import com.ps.realize.ui.createaddvideo.CreateAddVideoFragment;
import com.ps.realize.ui.cropImage.CropImageFragment;
import com.ps.realize.utils.AppListenerUtils;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.MediaUtils;
import com.ps.realize.utils.SharedMediaUtils;

public class CreateAddImageFragment extends Fragment implements IOnBackPressed, IPopBackDataListener {
    private final String TAG = CreateAddImageFragment.class.getSimpleName();
    private ActivityResultLauncher<Intent> imageFromLocalStorageActivity;
    private ActivityResultLauncher<Uri> imageFromCameraActivity;
    private Uri localImageCopyUri;
    private FragmentCreateAddImageBinding binding;
    private Fragment _this;
    private TextView tvNextBtn, tvReplaceBtn;
    private LinearLayout urlPopUp;
    private EditText etUrl;
    private LinearLayout addImageLL;
    private ImageView backBtn;
    private String targetImageURIString;
    private ImageView finalImageView;
    private LinearLayout addImageBottomBar, finalBtmViewBar;


    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
//        _this = this;
        AppListenerUtils.popBackDataHelper.addListener(this);
        attachActivityResultLaunchers();
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        _this = this;

        binding = FragmentCreateAddImageBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setViews();
        setListeners();

        if (savedInstanceState != null) {
            // Restore last state for target image.
            String imageUri = savedInstanceState.getString(Constants.TARGET_IMAGE_URI, null);
            if (imageUri != null) {
                targetImageURIString = imageUri;
            }
        }

        if (targetImageURIString != null) {
            setImage();
        } else {
            prepareViewForInitialView();
        }


        if (localImageCopyUri == null) {
            localImageCopyUri = SharedMediaUtils.createImageFile(MyApp.getContext());
        }
        return root;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(Constants.TARGET_IMAGE_URI, targetImageURIString);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void setListeners() {

    }

    private void setViews() {
        tvNextBtn = binding.createAddImgNextBtn;
        urlPopUp = binding.addImageLlUrlPopup;
        etUrl = binding.addImageEtUrl;

        addImageLL = binding.createAddImgLlAddBtn;
        backBtn = binding.createAddImgBackBtn;
        finalImageView = binding.finalImageView;
        addImageBottomBar = binding.createAddImgRlBottomBar;
        LinearLayout llCameraImage = binding.createAddImgCamera;
        LinearLayout llLocalImage = binding.createAddImgLocal;
        LinearLayout llUrl = binding.createAddImgUrl;

        finalBtmViewBar = binding.caifLlBtmFinalView;
        tvReplaceBtn = binding.caifTvReplace;


        addImageLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Load bottom sheet
                addImageBottomBar.setVisibility(View.VISIBLE);
            }
        });

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
                args.putString(Constants.TARGET_IMAGE_URI, targetImageURIString);
                frag.setArguments(args);
                FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                        R.id.main_fragment_holder,
                        frag,
                        CreateAddVideoFragment.class.getSimpleName());

            }
        });
        llCameraImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                imageFromCameraActivity.launch( getTempCameraImageUri());
                imageFromCameraActivity.launch(localImageCopyUri);
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
                    // Handle copying of image URI to local
                    urlPopUp.setVisibility(View.GONE);
                    handlePostImageCapture();
                    return false;  // Intentional to let the soft keyboard close
                }
                return false;
            }
        });

        tvReplaceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finalBtmViewBar.setVisibility(View.INVISIBLE);
                addImageBottomBar.setVisibility(View.VISIBLE);
            }
        });
    }

    /*
    private Uri getURI(File mediaFile) {
        return FileProvider.getUriForFile(getActivity().getApplicationContext(), getActivity().getPackageName() + ".provider", mediaFile);
    }
    */

    private void attachActivityResultLaunchers() {
        imageFromCameraActivity = registerForActivityResult(new ActivityResultContracts.TakePicture(), new ActivityResultCallback<Boolean>() {
            @Override
            public void onActivityResult(Boolean success) {
                if (success) {
                    handlePostImageCapture();
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
                                // copy to our local uri
                                MediaUtils.copyImage(MyApp.getContext(), imageUri, localImageCopyUri, new CallbackListener() {
                                    @Override
                                    public void onSuccess() {
                                        handlePostImageCapture();
                                    }

                                    @Override
                                    public void onFailure() {
                                        Log.e(TAG, "Failed to copy image to localImageCopyUri");
                                    }
                                });
//                                final InputStream imageStream = getContext().getContentResolver().openInputStream(imageUri);
//                                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);

                            } catch (Exception e) {
                                e.printStackTrace();

                            }
                        }
                    }
                });
    }

    private void handlePostImageCapture() {
        startCropRotateActivity();
    }

    private void setImage() {
        prepareViewForNextScreen();
        Glide.with(_this)
                .load(targetImageURIString)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(finalImageView);
    }

    private void prepareViewForNextScreen() {
        addImageLL.setVisibility(View.INVISIBLE);
        addImageBottomBar.setVisibility(View.INVISIBLE);
        tvNextBtn.setVisibility(View.VISIBLE);
        finalImageView.setVisibility(View.VISIBLE);
        finalBtmViewBar.setVisibility(View.VISIBLE);
    }

    private void prepareViewForInitialView() {
        tvNextBtn.setVisibility(View.INVISIBLE);
        finalImageView.setVisibility(View.GONE);
        finalBtmViewBar.setVisibility(View.INVISIBLE);
        addImageLL.setVisibility(View.VISIBLE);
        addImageBottomBar.setVisibility(View.INVISIBLE);
    }

    private void startCropRotateActivity() {
        CropImageFragment frag = new CropImageFragment();
        Bundle args = new Bundle();
        args.putString(Constants.DATA_PASS_CONSTANT_1, String.valueOf(localImageCopyUri));
        frag.setArguments(args);
        FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                R.id.main_fragment_holder,
                frag,
                frag.getClass().getSimpleName());
    }


    /*
    private Uri getTempCameraImageUri() {
        ///////////// OLD
        File imagePath = null;
        try {
            ContextWrapper cw = new ContextWrapper(getActivity().getApplicationContext());
            File directory = cw.getDir("imageDir", Context.MODE_PRIVATE);
            imagePath = new File(directory, "IMG_" + System.currentTimeMillis() + ".jpg");
            imagePath = File.createTempFile(
                    "IMG_",
                    ".jpg",
                    requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        cameraPhotoUri = getURI(imagePath);
        return cameraPhotoUri;
    }
    */

    @Override
    public boolean onBackPressed() {
        if (urlPopUp.getVisibility() == View.VISIBLE) {
            urlPopUp.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    /**
     * @param data
     */
    @Override
    public void onPopBackDataRecieved(String data) {
        targetImageURIString = data;
        setImage();


    }
}