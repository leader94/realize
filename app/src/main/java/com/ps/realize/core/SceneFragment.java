package com.ps.realize.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.ps.realize.R;
import com.ps.realize.core.components.CircleView;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.helperClasses.SceneFragmentHelper;
import com.ps.realize.databinding.FragmentScanBinding;
import com.ps.realize.ui.dashboard.DashboardFragment;
import com.ps.realize.utils.ARUtils;
import com.ps.realize.utils.Config;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.JSONUtils;
import com.ps.realize.utils.KeyboardUtils;

import java.util.List;

// TODO remove view from this fragment and convert it to a wrapper that handles only logics and does not have a view

/**
 * This fragment is meant to contain the app logic , passing appropriate data to the MyARFragment
 */
public class SceneFragment extends Fragment {
    private static final String TAG = SceneFragment.class.getSimpleName();
    private final int totalImageMappingsCount = 0;
    MyARFragment myARFragment;
    private Fragment _this;
    private FragmentScanBinding binding;
    private boolean mUserRequestedInstall = true;
    private CircleView arImageDetectedStatusCV;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        _this = this;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.i(TAG, "inside onCreateView");
        binding = FragmentScanBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "inside onViewCreated");
//        Glide.with(this).load("https://media.tenor.com/eSc2KWdhZPMAAAAj/parrot-party.gif").into(binding.sceneFragmentLoaderIV);
        checkARInstalled();
        if (FragmentUtils.bARSupported && FragmentUtils.bARInstalled) {
            if (myARFragment == null) {
                myARFragment = new MyARFragment();
            }
            List<ImageMapping> imageMappingList = SceneFragmentHelper.getImageMappings();
            if (imageMappingList != null) {
                String imageMappingListJSONString = JSONUtils.getGsonParser().toJson(imageMappingList);
                Bundle args = new Bundle();
                args.putString(Constants.IMAGE_MAPPING_LIST, imageMappingListJSONString);
                myARFragment.setArguments(args);
            }
            getChildFragmentManager().beginTransaction().add(R.id.arFragmentHolder, myARFragment).commit();
        }

        initialise(view);

    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    void checkARInstalled() {
        try {
            if (FragmentUtils.bARSupported) {
                switch (ArCoreApk.getInstance().requestInstall(getActivity(), mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success: Safe to create the AR session.
                        FragmentUtils.bARInstalled = true;
                        break;
                    case INSTALL_REQUESTED:
                        // When this method returns `INSTALL_REQUESTED`:
                        // 1. ARCore pauses this activity.
                        // 2. ARCore prompts the user to install or update Google Play
                        //    Services for AR (market://details?id=com.google.ar.core).
                        // 3. ARCore downloads the latest device profile data.
                        // 4. ARCore resumes this activity. The next invocation of
                        //    requestInstall() will either return `INSTALLED` or throw an
                        //    exception if the installation or update did not succeed.
                        mUserRequestedInstall = false;
                        break;
                }
            }
        } catch (UnavailableUserDeclinedInstallationException e) {
            // Display an appropriate message to the user and return gracefully.
        } catch (Exception e) {
            // mSession remains null, since session creation has failed.
        }
    }

    void initialise(View view) {
        initialiseViewsItems(view);
    }

    void initialiseViewsItems(View view) {
        ImageView backBtn = binding.arFragmentBackBtn;
        arImageDetectedStatusCV = binding.fragmentSceneStatusCircleView;
        arImageDetectedStatusCV.setColor(getResources().getColor(R.color.grey));
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Config.allowARSceneBackgroundLoad) {
                    // set pixel size 0
                    ARUtils.hideSceneFragment();
                    // add DashboardFragment
                    FragmentUtils.addFragment((AppCompatActivity) getActivity(), R.id.main_fragment_holder, new DashboardFragment(), DashboardFragment.class.getSimpleName());
                } else {
                    // back press
                    KeyboardUtils.backPress(_this);
                }
            }
        });
    }


    private void showARFragment() {
//        binding.sceneFragmentLoaderIV.setVisibility(View.GONE);
//        binding.sceneFragmentLL.setVisibility(View.GONE);

        ViewGroup.LayoutParams layoutParams = binding.arFragmentHolder.getLayoutParams();

        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;

        binding.arFragmentHolder.setLayoutParams(layoutParams);

    }


    public void onAugmentedImageUpdateListener(AugmentedImage augmentedImage) {
        AugmentedImage.TrackingMethod trackingMethod = augmentedImage.getTrackingMethod();
        TrackingState trackingState = augmentedImage.getTrackingState();

        int color = getResources().getColor(R.color.grey);
        switch (trackingState) {
            case STOPPED:
                color = getResources().getColor(R.color.black);
                break;
            case PAUSED:
                if (trackingMethod == AugmentedImage.TrackingMethod.NOT_TRACKING) {
                    color = getResources().getColor(R.color.light_orange);
                } else if (trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    color = getResources().getColor(R.color.orange);
                }
                break;
            case TRACKING:
                if (trackingMethod == AugmentedImage.TrackingMethod.LAST_KNOWN_POSE) {
                    color = getResources().getColor(R.color.celadon);
                } else if (trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
                    color = getResources().getColor(R.color.neon_green);
                }
                break;
            default:
                color = getResources().getColor(R.color.grey);
        }


        arImageDetectedStatusCV.setColor(color);
    }
}
