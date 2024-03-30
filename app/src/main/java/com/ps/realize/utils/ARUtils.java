package com.ps.realize.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.ImageInsufficientQualityException;
import com.ps.realize.MyApp;
import com.ps.realize.R;
import com.ps.realize.core.SceneFragment;
import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.helperClasses.ARCoreSessionHelper;
import com.ps.realize.core.helperClasses.ArFragmentHelper;
import com.ps.realize.core.helperClasses.AugmentedImageDatabaseHelper;
import com.ps.realize.core.helperClasses.Counter;
import com.ps.realize.core.helperClasses.SceneFragmentHelper;

import java.util.List;

public class ARUtils {
    private static final String TAG = ARUtils.class.getSimpleName();
    private static final float imageSize = 0.20f;
    public static ArFragmentHelper arFragmentHelper = new ArFragmentHelper();
    public static Counter loadedImageMappingsCounter = new Counter();
    public static boolean isSceneFragmentLoaded = false;
    private static AugmentedImageDatabase augmentedImageDatabase;
    private static int totalImageMappingsCount = 0;

    public static int getTotalImageMappingsCount() {
        return totalImageMappingsCount;
    }

    public static void setTotalImageMappingsCount(int totalImageMappingsCount) {
        ARUtils.totalImageMappingsCount = totalImageMappingsCount;
    }

    public static int getLoadedImageMappings() {
        return loadedImageMappingsCounter.getCount();
    }


    public static void initAugmentedImageDatabase(Activity activity) {
        Session session = ARCoreSessionHelper.createArCoreSession(activity);
        augmentedImageDatabase = new AugmentedImageDatabase(session);

    }

    public static AugmentedImageDatabase getAugmentedImageDatabase(Activity activity) {

        if (augmentedImageDatabase == null) {
            initAugmentedImageDatabase(activity);
        }


        return augmentedImageDatabase;
    }

    public static boolean loadAugmentedImageDatabaseFromFile(Activity activity) {
        Session session = ARCoreSessionHelper.createArCoreSession(activity);
        try {
            augmentedImageDatabase = AugmentedImageDatabaseHelper.loadDatabase(MyApp.getContext(), session);
            return true;
        } catch (Exception e) {
            Log.w(TAG, "loadAugmentedImageDatabaseFromFile failed");
            return false;
        }
    }

    public static void showSceneFragment() {
//        MyARFragment.resumeARCoreSession();
        ViewGroup.LayoutParams layoutParams = LocalData.activityMainBinding.fragmentHolderForAR.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        LocalData.activityMainBinding.fragmentHolderForAR.setLayoutParams(layoutParams);
    }

    public static void hideSceneFragment() {
        ViewGroup.LayoutParams layoutParams = LocalData.activityMainBinding.fragmentHolderForAR.getLayoutParams();
        layoutParams.width = 0;
        layoutParams.height = 0;
        LocalData.activityMainBinding.fragmentHolderForAR.setLayoutParams(layoutParams);
//        MyARFragment.pauseARCoreSession();
    }

    public static void ArFragmentShown() {
        arFragmentHelper.notifyListeners();
    }


    public static void recreateAugmentedImageDatabase(Activity activity, Context context) {
        List<ImageMapping> imageMappingList = SceneFragmentHelper.getImageMappings();
        totalImageMappingsCount = imageMappingList.size();
        loadedImageMappingsCounter.reset();

        for (ImageMapping imageMapping : imageMappingList) {
            Log.i(TAG, "CCC: " + imageMapping);
            ImageObj imageToBeTracked = imageMapping.getImage();

            try {
                Bitmap bitmaplocal = MediaUtils.getBitmap(activity, context, Uri.parse(imageToBeTracked.getLocalUrl()));
                if (bitmaplocal != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap targetBmp = bitmaplocal.copy(Bitmap.Config.ARGB_8888, false);
                            try {
                                augmentedImageDatabase.addImage(imageToBeTracked.getId(), targetBmp, imageSize);
                                Log.i(TAG, "CCC: LocalUrl adding image " + imageToBeTracked.getId());
                            } catch (ImageInsufficientQualityException e) {
                                Log.e(TAG, "Image Quality Issue", e);
                            } catch (Exception e) {
                                Log.e(TAG, "Error: Adding image to AiDb", e);
                            }
                            loadedImageMappingsCounter.increment();
                        }
                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "CCC: Image not found at the LocalUrl ");

                Glide.with(context)
                        .asBitmap()
                        .load(imageToBeTracked.getUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                                Bitmap matrixImage = resource;
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            augmentedImageDatabase.addImage(imageToBeTracked.getId(), matrixImage, imageSize);
                                            Log.i(TAG, "CCC: URL adding image " + imageToBeTracked.getId());
                                        } catch (ImageInsufficientQualityException e) {
                                            Log.e(TAG, "Image Quality Issue", e);
                                        } catch (Exception e) {
                                            Log.e(TAG, "Error: Adding image to AiDb", e);
                                        }
                                        loadedImageMappingsCounter.increment();
                                    }
                                }).start();
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                loadedImageMappingsCounter.increment();
                                Log.i(TAG, " CCC: Failed to load remote base image");
                            }
                        });
            }
        }
    }


    public static void addImageToAugmentedImageDatabase(Activity activity, Context context, ImageObj imageToBeTracked) {

        totalImageMappingsCount = totalImageMappingsCount + 1;
        try {
            Bitmap bitmaplocal = MediaUtils.getBitmap(activity, context, Uri.parse(imageToBeTracked.getLocalUrl()));
            if (bitmaplocal != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Bitmap targetBmp = bitmaplocal.copy(Bitmap.Config.ARGB_8888, false);
                        // Every image has to have its own unique String identifier
                        augmentedImageDatabase.addImage(imageToBeTracked.getId(), targetBmp, imageSize);

                        Log.i(TAG, "CCC: LocalUrl adding image " + imageToBeTracked.getId());


                        loadedImageMappingsCounter.increment();
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "CCC: Image not found at the LocalUrl ");

            Glide.with(context)
                    .asBitmap()
                    .load(imageToBeTracked.getUrl())
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable com.bumptech.glide.request.transition.Transition<? super Bitmap> transition) {
                            Bitmap matrixImage = resource;

                            // The following line takes time and must be executed on diffrent thread
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    augmentedImageDatabase.addImage(imageToBeTracked.getId(), matrixImage, imageSize);
                                    Log.i(TAG, "CCC: URL adding image " + imageToBeTracked.getId());

                                    loadedImageMappingsCounter.increment();
                                }
                            }).start();
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            loadedImageMappingsCounter.increment();
                            Log.i(TAG, " CCC: Failed to load remote base image");
                        }
                    });
        }
    }


    public static void loadSceneFragment(AppCompatActivity activity) {
        FragmentUtils.addFragment(activity, R.id.fragment_holder_for_AR, new SceneFragment(), SceneFragment.class.getSimpleName());
        isSceneFragmentLoaded = true;
    }

    public static void removeSceneFragment(AppCompatActivity activity) {
        FragmentUtils.removeFragment(activity, SceneFragment.class.getSimpleName());
        isSceneFragmentLoaded = false;
    }
}
