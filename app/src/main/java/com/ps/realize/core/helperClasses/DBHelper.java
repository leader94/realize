package com.ps.realize.core.helperClasses;

import android.app.Activity;

import com.ps.realize.AppDatabase;
import com.ps.realize.core.daos.user.UserDao;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.json.ProjectObj;
import com.ps.realize.core.interfaces.CallbackListener;

import java.util.ArrayList;

public class DBHelper {

    public static void updateUserProjectsInDB(Activity activity, CallbackListener listener) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ArrayList<ProjectObj> projectObjs = new ArrayList<>();
                    projectObjs.add(LocalData.curProject);
                    LocalData.curUser.setProjects(projectObjs);
                    AppDatabase db = AppDatabase.getInstance(activity.getApplicationContext());
                    UserDao userDao = db.userDao();
                    userDao.insertAll(LocalData.curUser);
                    listener.onSuccess();
                } catch (Exception e) {
                    e.printStackTrace();
                    listener.onFailure();
                }
            }
        }).start();
    }
}



