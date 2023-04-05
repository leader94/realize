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
import com.ps.realize.databinding.FragmentScanBinding;
import com.ps.realize.utils.CommonService;

public class SceneFragment extends Fragment {
    private static final String TAG = SceneFragment.class.getSimpleName();

    private FragmentScanBinding binding;
    MyARFragment myARFragment;
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
        if (CommonService.bARSupported && CommonService.bARInstalled) {
            myARFragment = new MyARFragment();
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
            if (CommonService.bARSupported) {
                switch (ArCoreApk.getInstance().requestInstall(getActivity(), mUserRequestedInstall)) {
                    case INSTALLED:
                        // Success: Safe to create the AR session.
                        CommonService.bARInstalled = true;
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

}
