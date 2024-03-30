package com.ps.realize;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.ar.core.ArCoreApk;
import com.google.gson.Gson;
import com.ps.realize.core.MyARFragment;
import com.ps.realize.core.daos.user.UserDao;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.User;
import com.ps.realize.core.interfaces.IOnBackPressed;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.ActivityMainBinding;
import com.ps.realize.ui.dashboard.DashboardFragment;
import com.ps.realize.utils.ARUtils;
import com.ps.realize.utils.CommonAppUtils;
import com.ps.realize.utils.Config;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.NetworkUtils;
import com.ps.realize.utils.PreferencesUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private static AppCompatActivity self;
    private final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_CAMERA_CODE = 100;
    private ActivityMainBinding binding;
    private User user;

    public static AppCompatActivity getMainActivity() {
        return self;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "On create called");
        // getSupportActionBar().hide();
        self = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        LocalData.activityMainBinding = binding;
        setContentView(binding.getRoot());

        checkARSupport();
        addPermissions();

        startAsyncWork();


        FragmentUtils.addFragment(self, R.id.main_fragment_holder, new DashboardFragment(), DashboardFragment.class.getSimpleName());


        if (Config.allowARSceneBackgroundLoad) {

            if (!ARUtils.loadAugmentedImageDatabaseFromFile(getMainActivity())) {
                ARUtils.initAugmentedImageDatabase(getMainActivity());
                ARUtils.recreateAugmentedImageDatabase(getMainActivity(), getApplicationContext());
            }

            ARUtils.loadSceneFragment(self);
        }


    }

    @Override
    public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.main_fragment_holder);
        if (!(fragment instanceof IOnBackPressed) || !((IOnBackPressed) fragment).onBackPressed()) {
            super.onBackPressed();

            // not needed, keeping for future use maybe
//            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
//                getSupportFragmentManager().popBackStack();
//            } else {
//                super.onBackPressed();
//            }
        }
    }

    private void checkARSupport() {
        Log.i("MyTag", "inside checkARSupport");
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Continue to query availability at 5Hz while compatibility is checked in the background.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    checkARSupport();
                }
            }, 200);
        }
        if (availability.isSupported()) {
            Log.i("MyTag", "bARSupported true");
            FragmentUtils.bARSupported = true;
        } else { // The device is unsupported or unknown.
            FragmentUtils.bARSupported = false;
        }
    }

    private void startAsyncWork() {
        new Thread(new Runnable() {
            public void run() {
                getConfiguration();
            }
        }).start();

    }

    private void getConfiguration() {


        try {
            String userId = PreferencesUtils.getUserId(getMainActivity());
            User user = null;
            String token = "";
            if (userId != null) {
                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                UserDao userDao = db.userDao();
                user = userDao.getUserById(userId);
            }
            if (user != null) {
                LocalData.curUser = user;
                LocalData.curUser.setToken(user.getToken());
                CommonAppUtils.setDefaultProject();

                token = user.getToken();
            } else {
                userId = NetworkUtils.userId;
                token = NetworkUtils.authToken;
            }


            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("details", "true");
            NetworkUtils.getWithToken("/users/" + userId, queryParams,
                    token,
                    new NetworkListener() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            Log.e(TAG, "Failed to get user details");
                        }

                        @Override
                        public void onResponse(Response response) {
                            try {
                                String userJSONStr = response.body().string();
                                if (userJSONStr == null) return;
                                Gson gson = new Gson();
                                User user = gson.fromJson(userJSONStr, User.class);
                                LocalData.curUser = user;
                                LocalData.curUser.setToken(user.getToken());

                                PreferencesUtils.setUserId(getMainActivity(), user.getId());
                                AppDatabase db = AppDatabase.getInstance(getApplicationContext());
                                UserDao userDao = db.userDao();
                                userDao.insertAll(user);
                                CommonAppUtils.setDefaultProject();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }


                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void addPermissions() {
        String[] permissions = new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS,
                Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO, Manifest.permission.READ_MEDIA_AUDIO
        };


        List<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_CAMERA_CODE);
        } else {
            // All permissions are already granted, proceed with the operation
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "On pause called");
        MyARFragment.pauseARCoreSession();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "On resume called");
        MyARFragment.resumeARCoreSession();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "On destroy called");
        MyARFragment.destroyArCoreSession();

    }
}