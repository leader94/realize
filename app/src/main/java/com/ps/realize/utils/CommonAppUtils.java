package com.ps.realize.utils;

import android.util.Log;

import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.json.ProjectObj;

import java.util.ArrayList;
import java.util.UUID;

public class CommonAppUtils {
    private static final String TAG = CommonAppUtils.class.getSimpleName();
//    public Uri getURI(File mediaFile, Fragment fragment) {
//        return FileProvider.getUriForFile(fragment.getActivity().getApplicationContext(), fragment.getActivity().getPackageName() + ".provider", mediaFile);
//    }

    public static String generateUuid() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }


    // Created for future delete if unused
    public static UUID stringToUuid(String uuid) {
        return UUID.fromString(uuid);
    }


    public static void setDefaultProject() {
        ArrayList<ProjectObj> projects = LocalData.curUser.getProjects();

        ProjectObj firstProject = null;
        String projectId = null;
        try {
            firstProject = projects.get(0);
//                    projects.getJSONObject(0); // TODO update this
            LocalData.curProject = firstProject;


        } catch (Exception e) {
            Log.e(TAG, "Failed to set Default Project ", e);
            e.printStackTrace();
        }

    }
}
