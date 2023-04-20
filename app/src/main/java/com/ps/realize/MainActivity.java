package com.ps.realize;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.ar.core.ArCoreApk;
import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.User;
import com.ps.realize.core.interfaces.IOnBackPressed;
import com.ps.realize.core.interfaces.NetworkListener;
import com.ps.realize.databinding.ActivityMainBinding;
import com.ps.realize.ui.dashboard.DashboardFragment;
import com.ps.realize.utils.CommonService;
import com.ps.realize.utils.JSONUtils;
import com.ps.realize.utils.NetworkUtils;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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

        CommonService.addFragment(self, R.id.main_fragment_holder, new DashboardFragment(), DashboardFragment.class.getSimpleName());
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
            CommonService.bARSupported = true;
        } else { // The device is unsupported or unknown.
            CommonService.bARSupported = false;
        }
    }

    private void getConfiguration() {
        try {
            NetworkUtils.get("/user/12466434534", new NetworkListener() {
                @Override
                public void onFailure(Request request, IOException e) {
                    Log.e(TAG, "Failed to get user details");
                }

                @Override
                public void onResponse(Response response) {
                    JSONObject userJSON = JSONUtils.getJSONObject(response);
                    if (userJSON == null) return;
                    LocalData.curUser.setUserObject(userJSON);

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void checkAndroidPermissions() {
        //
        String[] permissions = {Manifest.permission.CAMERA, Manifest.permission.INTERNET, Manifest.permission.WRITE_EXTERNAL_STORAGE};
//        String rationale = "Please provide location permission so that you can ...";
//
//        Permissions.Options options = new Permissions.Options()
//                .setRationaleDialogTitle("Info")
//                .setSettingsDialogTitle("Warning");

        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @Override
            public void onGranted() {
                // do your task.
                Toast.makeText(MainActivity.this, "Camera+Storage granted.", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
                // permission denied, block the feature.
            }
        });
    }

    private void addPermissions() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.MANAGE_EXTERNAL_STORAGE
            }, REQUEST_CAMERA_CODE);
        }

    }
}