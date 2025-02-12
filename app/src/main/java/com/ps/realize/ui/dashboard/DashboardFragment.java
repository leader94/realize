package com.ps.realize.ui.dashboard;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.ps.realize.MainActivity;
import com.ps.realize.R;
import com.ps.realize.core.SceneFragment;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.interfaces.IKeyboardListener;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.FragmentDashboardBinding;
import com.ps.realize.ui.createaddimage.CreateAddImageFragment;
import com.ps.realize.ui.directory.DirectoryFragment;
import com.ps.realize.utils.ARUtils;
import com.ps.realize.utils.Config;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.JSONUtils;
import com.ps.realize.utils.KeyboardUtils;
import com.ps.realize.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Request;
import okhttp3.Response;

public class DashboardFragment extends Fragment {
    private final String TAG = DashboardFragment.class.getSimpleName();
    private final KeyboardUtils keyboardUtils = new KeyboardUtils();
    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private Fragment _this;
    private SearchView searchView;
    private LinearLayout llCreateBtn, llDirectoryBtn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        _this = this;

        getConfiguration();
        setViews();
        setOnClickListeners();
        setKeyBoardListener();
        return root;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        keyboardUtils.removeKeyboardListener();
    }

    private void setViews() {
        System.out.println("Inside Setviews");
        final ImageView profilePhoto = binding.dashboardIvProfilePhoto;

        LocalData.curUser.setObjectUpdateListener(obj ->
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(_this).load(obj.getProfilePhoto()).circleCrop().into(profilePhoto);
                    }
                })

        );
        Glide.with(this).load(dashboardViewModel.getProfilePhoto()).circleCrop().into(profilePhoto);
        Glide.with(this).load(dashboardViewModel.getSuggestionLaneIV("id1_i1")).into(binding.dashboardIvId1I1);
        Glide.with(this).load(dashboardViewModel.getSuggestionLaneIV("id1_i2")).into(binding.dashboardIvId1I2);
//        Glide.with(this).load(dashboardViewModel.getSuggestionLaneIV("id2_i1")).into(binding.dashboardIvId2I1);
        Glide.with(this).load(dashboardViewModel.getSuggestionLaneIV("id2_i2")).into(binding.dashboardIvId2I2);

        searchView = binding.dashboardSvHomepageSearch;
        searchView.setOnQueryTextFocusChangeListener((view, focus) -> {
            if (!focus) {
                keyboardUtils.hideKeyboard(searchView);
            }
        });

        llCreateBtn = binding.dashboardLlCreateBtn;
        llDirectoryBtn = binding.dashboardLlDirectoryBtn;


    }

    private void setOnClickListeners() {
        LinearLayout scannerBtn = binding.dashboardLlScannerBtn;
        scannerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Config.allowARSceneBackgroundLoad) {
                    FragmentUtils.removeFragment((
                            AppCompatActivity) getActivity(), DashboardFragment.class.getSimpleName());
                    if (!ARUtils.isSceneFragmentLoaded) {
                        ARUtils.loadSceneFragment(MainActivity.getMainActivity());
                    }
                    ARUtils.showSceneFragment();
                    ARUtils.ArFragmentShown();

                } else {
                    FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                            R.id.main_fragment_holder,
                            new SceneFragment(),
                            SceneFragment.class.getSimpleName());
                }


            }
        });

        llCreateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARUtils.removeSceneFragment(MainActivity.getMainActivity());

//                FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
//                        R.id.main_fragment_holder,
//                        new CreateAddVideoFragment(),
//                        CreateAddVideoFragment.class.getSimpleName());
                FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                        R.id.main_fragment_holder,
                        new CreateAddImageFragment(),
                        CreateAddImageFragment.class.getSimpleName());
            }
        });

        llDirectoryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ARUtils.removeSceneFragment(MainActivity.getMainActivity());
                FragmentUtils.replaceFragment((AppCompatActivity) getActivity(),
                        R.id.main_fragment_holder,
                        new DirectoryFragment(),
                        DirectoryFragment.class.getSimpleName());
            }
        });
    }

    private void getConfiguration() {

        try {
            NetworkUtils.get("/configs/home", null, new NetworkListener() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Failed to get home details");
                }

                @Override
                public void onResponse(Response response) {
                    System.out.println("Inside response of configuration");

                    JSONObject configJSON = JSONUtils.getJSONObject(response);
                    if (configJSON == null) return;
                    if (getActivity() == null) return;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (dashboardViewModel == null || binding == null) return;
                            dashboardViewModel.updateUI(configJSON, binding.dashboardLlSuggestions);
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setKeyBoardListener() {
        RelativeLayout bottomBar = binding.dashboardRlBottomBar;
        keyboardUtils.initKeyBoardListener(getActivity(), new IKeyboardListener() {
            @Override
            public void onKeyboardShow() {
                bottomBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onKeyboardHide() {
                bottomBar.setVisibility(View.VISIBLE);
                searchView.clearFocus();
            }
        });
    }


}