package com.ps.realize.core;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.ps.realize.R;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.datamodels.ar.VideoObj;
import com.ps.realize.core.datamodels.json.BaseObj;
import com.ps.realize.core.datamodels.json.OverlayObj;
import com.ps.realize.core.datamodels.json.ProjectObj;
import com.ps.realize.core.datamodels.json.SceneObj;
import com.ps.realize.databinding.FragmentScanBinding;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.JSONUtils;

import java.util.ArrayList;
import java.util.List;

// TODO remove view from this fragment and convert it to a wrapper that handles only logics and does not have a view

/**
 * This fragment is meant to contain the app logic , passing appropriate data to the MyARFragment
 */
public class SceneFragment extends Fragment {
    private static final String TAG = SceneFragment.class.getSimpleName();
    private final Constants constants = new Constants();
    MyARFragment myARFragment;
    private FragmentScanBinding binding;
    private boolean mUserRequestedInstall = true;

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
        checkARInstalled();
        if (FragmentUtils.bARSupported && FragmentUtils.bARInstalled) {
            myARFragment = new MyARFragment();
            List<ImageMapping> imageMappingList = getImageMappings();
            if (imageMappingList != null) {
                String imageMappingListJSONString = JSONUtils.getGsonParser().toJson(imageMappingList);
                Bundle args = new Bundle();
                args.putString(constants.IMAGE_MAPPING_LIST, imageMappingListJSONString);
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

    }

    List<ImageMapping> getImageMappings() {

        List<ImageMapping> imageMappingList = new ArrayList();
        ProjectObj project = LocalData.curProject;

        try {
            List<SceneObj> scenes = project.getScenes();
            scenes.forEach((scene) -> {
                OverlayObj overlayObj = scene.getOverlays().get(0);
                BaseObj baseObj = scene.getBases().get(0);
                ImageObj image = new ImageObj(baseObj.getId(),
                        baseObj.getUploadUrl() + "/" + baseObj.getFileName(),
                        baseObj.getLocalPath());
                VideoObj video = new VideoObj(overlayObj.getId(),
                        overlayObj.getUploadUrl() + "/" + overlayObj.getFileName(),
                        overlayObj.getLocalPath());
                List<VideoObj> videoList = new ArrayList<>();
                videoList.add(video);
                ImageMapping imageMapping = new ImageMapping(videoList, image);
                imageMappingList.add(imageMapping);
                Log.i(TAG, "BBB: adding image " + image.getUrl());
            });

//            SceneObj scene = scenes.get(0);

        } catch (Exception e) {
            Log.e(TAG, " Failed getting ImageMappings ", e);
        }
        return imageMappingList;
    }


}
