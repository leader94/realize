package com.ps.realize.core.data;

import com.ps.realize.core.datamodels.User;
import com.ps.realize.core.datamodels.json.ProjectObj;
import com.ps.realize.databinding.ActivityMainBinding;

public class LocalData {
    public static User curUser = new User();
    public static ProjectObj curProject = null;

    public static ActivityMainBinding activityMainBinding = null;
}
