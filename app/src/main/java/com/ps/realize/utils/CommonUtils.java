package com.ps.realize.utils;

import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;

public class CommonUtils {
    public Uri getURI(File mediaFile, Fragment fragment) {
        return FileProvider.getUriForFile(fragment.getActivity().getApplicationContext(), fragment.getActivity().getPackageName() + ".provider", mediaFile);
    }
}
