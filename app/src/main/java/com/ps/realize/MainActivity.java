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
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.User;
import com.ps.realize.core.interfaces.IOnBackPressed;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.ActivityMainBinding;
import com.ps.realize.ui.dashboard.DashboardFragment;
import com.ps.realize.utils.CommonAppUtils;
import com.ps.realize.utils.FragmentUtils;
import com.ps.realize.utils.NetworkUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {
    private final String TAG = MainActivity.class.getSimpleName();
    private final int REQUEST_CAMERA_CODE = 100;
    

    private ActivityMainBinding binding;
    private User user;
    private AppCompatActivity self;

    public AppCompatActivity getMainActivity() {
        return this;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();
        self = this;
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        checkARSupport();
        addPermissions();

        getConfiguration();

        FragmentUtils.addFragment(self, R.id.main_fragment_holder, new DashboardFragment(), DashboardFragment.class.getSimpleName());


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

    private void getConfiguration() {
        try {
            Map<String, String> queryParams = new HashMap<>();
            queryParams.put("details", "true");

            NetworkUtils.getWithToken("/users/74f28839-64db-4e2f-ad54-9647b380894d", queryParams,
                    NetworkUtils.authToken,
                    new NetworkListener() {
                        @Override
                        public void onFailure(Request request, IOException e) {
                            Log.e(TAG, "Failed to get user details");
                        }

                        @Override
                        public void onResponse(Response response) {
//                            JSONObject userJSON = JSONUtils.getJSONObject(response);

                            try {
                                String userJSONStr = response.body().string();
                                if (userJSONStr == null) return;
                                Gson gson = new Gson();
                                User user = gson.fromJson(userJSONStr, User.class);
                                LocalData.curUser = user;
                                LocalData.curUser.setToken(NetworkUtils.authToken);
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
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE, Manifest.permission.POST_NOTIFICATIONS
            }, REQUEST_CAMERA_CODE);
        }


    }
}