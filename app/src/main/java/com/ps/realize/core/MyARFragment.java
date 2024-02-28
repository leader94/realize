package com.ps.realize.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.filament.Engine;
import com.google.android.filament.filamat.MaterialBuilder;
import com.google.android.filament.filamat.MaterialPackage;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Sceneform;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.EngineInstance;
import com.google.ar.sceneform.rendering.ExternalTexture;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.rendering.RenderableInstance;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.BaseArFragment;
import com.google.ar.sceneform.ux.InstructionsController;
import com.google.ar.sceneform.ux.TransformableNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.ps.realize.MainActivity;
import com.ps.realize.MyApp;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.helperClasses.ArFragmentHelper;
import com.ps.realize.core.helperClasses.AugmentedImageDatabaseHelper;
import com.ps.realize.core.helperClasses.MapDetails;
import com.ps.realize.core.helperClasses.SceneFragmentHelper;
import com.ps.realize.core.interfaces.IArFragmentListener;
import com.ps.realize.core.interfaces.ICounterListener;
import com.ps.realize.utils.ARUtils;
import com.ps.realize.utils.Constants;
import com.ps.realize.utils.MediaUtils;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * This Fragment is to be generic and will work based on configurations recieved and will not have any app logic.
 * Pass everything as configuration to this fragment
 */

public class MyARFragment extends ArFragment implements BaseArFragment.OnSessionConfigurationListener, ICounterListener, IArFragmentListener {
    private static final String TAG = MyARFragment.class.getSimpleName();
    private static Session arSession;
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();
    private final Constants constants = new Constants();
    private final Map<String, MapDetails> _map = new HashMap<>();
    SceneFragment parentFragment;
    private Config arConfig;
    private List<ImageMapping> imageMappingList;
    private AugmentedImageDatabase database;
    private Renderable plainVideoModel;
    private Material plainVideoMaterial;

    public static void pauseARCoreSession() {
        try {
            if (arSession != null) {
                arSession.pause();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while pausing session", e);
        }

    }

    public static void resumeARCoreSession() {
        try {
            if (arSession != null) {
                arSession.resume();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error while resuming session", e);
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setOnSessionConfigurationListener(this);

        ARUtils.loadedImageMappingsCounter.addListener(this);
        ARUtils.arFragmentHelper.addListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Get reference to the parent fragment
        parentFragment = (SceneFragment) getParentFragment();

        if (com.ps.realize.utils.Config.allowARSceneBackgroundLoad) {
            imageMappingList = SceneFragmentHelper.getImageMappings();
            createMap();
        } else {
            setupViewModelsAndCreateARImageDB();
        }
    }

    public void setupViewModelsAndCreateARImageDB() {
        Bundle args = getArguments();

        String imageMappingJSONString = args.getString(Constants.IMAGE_MAPPING_LIST);
        if (imageMappingJSONString != null) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<ArrayList<ImageMapping>>() {
            }.getType();
            imageMappingList = gson.fromJson(imageMappingJSONString, userListType);

            createMap();
        }

        if (Sceneform.isSupported(MyApp.getContext())) {
            if (imageMappingList != null) {
                Log.i(TAG, "Loading matrix and material");
                // .glb models can be loaded at runtime when needed or when app starts
                loadMatrixModel();
                loadMatrixMaterial();
            }
        }

        setupARImageDBAsync();

    }

    private void setupARImageDBAsync() {

//        config.setAugmentedImageDatabase(ARUtils.getAugmentedImageDatabase(getActivity()));
//        session.configure(config);

        new Thread(new Runnable() {
            @Override
            public void run() {
                // Your background task
                Log.i(TAG, "Handling loading in BG");

                // Images to be detected by our AR need to be added in AugmentedImageDatabase
                // This is how database is created at runtime
                // You can also prebuild database in you computer and load it directly (see: https://developers.google.com/ar/develop/java/augmented-images/guide#database)
                database = new AugmentedImageDatabase(arSession);
                addImagesToDatabase(arSession, arConfig);
            }
        }).start();


    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        futures.forEach(future -> {
            if (!future.isDone())
                future.cancel(true);
        });

        _map.forEach((key, value) -> {
            if (value.mediaPlayer != null) {
                value.mediaPlayer.stop();
                value.mediaPlayer.reset();
            }
            if (value.exoPlayer != null) {
                value.exoPlayer.stop();
                value.exoPlayer.release();
            }
        });
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        arSession = session;
        arConfig = config;

        setupARConfigs();


        try {
            session.resume();
            session.pause();
            session.resume();
        } catch (Exception e) {
            Log.e(TAG, "Error while resume and pausing session", e);
        }

        // Disable plane detection
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        if (Sceneform.isSupported(MyApp.getContext())) {
            loadMatrixModel();
            loadMatrixMaterial();
        }
    }

    private void addImagesToDatabase(Session session, Config config) {
        if (imageMappingList == null) {
            Log.i(TAG, "imageMappingList is NULL");
            return;
        }
        ARUtils.setTotalImageMappingsCount(imageMappingList.size());

        // TODO update this to make the following 2 line called only once as improvement instead of being called each time a image is added to db
//        config.setAugmentedImageDatabase(database);
//        session.configure(config);

        for (ImageMapping imageMapping : imageMappingList) {
            Log.i(TAG, "AAA: " + imageMapping);
            ImageObj imageToBeTracked = imageMapping.getImage();

            try {
                Bitmap bitmaplocal = MediaUtils.getBitmap(MainActivity.getMainActivity(), MyApp.getContext(), Uri.parse(imageToBeTracked.getLocalUrl()));
                if (bitmaplocal != null) {
                    // The following line takes time and must be executed on diffrent thread
//                    Bitmap targetBmp = bitmaplocal.copy(Bitmap.Config.ARGB_8888, false);
//                    database.addImage(imageToBeTracked.getId(), targetBmp);
//
////                            config.setAugmentedImageDatabase(database);
////                            session.configure(config);
//                    Log.i(TAG, "BBB: LocalUrl adding image " + imageToBeTracked.getId());
//                    counter.increment();
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap targetBmp = bitmaplocal.copy(Bitmap.Config.ARGB_8888, false);
                            // Every image has to have its own unique String identifier
                            database.addImage(imageToBeTracked.getId(), targetBmp);

//                            config.setAugmentedImageDatabase(database);
//                            session.configure(config);
                            Log.i(TAG, "BBB: LocalUrl adding image " + imageToBeTracked.getId());
//                            counter.increment();
                            ARUtils.loadedImageMappingsCounter.increment();
                        }

                    }).start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "Image not found at the LocalUrl ");

                Glide.with(this)
                        .asBitmap()
                        .load(imageToBeTracked.getUrl())
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                Bitmap matrixImage = resource;
                                Log.i(TAG, "inside onResourceReady1");
                                // Every image has to have its own unique String identifier
                                // The following line takes time and must be executed on diffrent thread
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        database.addImage(imageToBeTracked.getId(), matrixImage);

                                        Log.i(TAG, "BBB: URL adding image " + imageToBeTracked.getId());
//                                        config.setAugmentedImageDatabase(database);
//                                        session.configure(config);
//                                        counter.increment();
                                        ARUtils.loadedImageMappingsCounter.increment();
                                    }
                                }).start();

                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                Log.i(TAG, "Failed to load remote base image");
//                                counter.increment();
                                ARUtils.loadedImageMappingsCounter.increment();

                            }
                        });
            }
        }
        // Check for image detection
        setOnAugmentedImageUpdateListener(MyARFragment.this::onAugmentedImageTrackingUpdate);
    }

    private void loadMatrixModel() {
        loadVideoMatrixModel();
    }

    private void loadVideoMatrixModel() {
        futures.add(ModelRenderable.builder()
                .setSource(MyApp.getContext(), Uri.parse("models/Video.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(model -> {
                    //removing shadows for this Renderable
                    model.setShadowCaster(false);
                    model.setShadowReceiver(true);
                    plainVideoModel = model;
                    Log.i(TAG, "Renderable loaded");
                })
                .exceptionally(
                        throwable -> {
                            Log.e(TAG, "Unable to load renderable", throwable);

                            return null;
                        }));
    }

    private void loadMatrixMaterial() {
        Engine filamentEngine = EngineInstance.getEngine().getFilamentEngine();
        loadVideoMatrixMaterial(filamentEngine);
    }

    private void loadVideoMatrixMaterial(Engine filamentEngine) {
        Log.i(TAG, "111 Loading matrix loadVideoMatrixMaterial");
        MaterialBuilder.init();
        MaterialBuilder materialBuilder = new MaterialBuilder()
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("External Video Material")
                .require(MaterialBuilder.VertexAttribute.UV0)
                .shading(MaterialBuilder.Shading.UNLIT)
                .doubleSided(true)
                .samplerParameter(MaterialBuilder.SamplerType.SAMPLER_EXTERNAL,
                        MaterialBuilder.SamplerFormat.FLOAT,
                        MaterialBuilder.ParameterPrecision.DEFAULT,
                        "videoTexture")
                .optimization(MaterialBuilder.Optimization.NONE);

        MaterialPackage plainVideoMaterialPackage = materialBuilder
                .blending(MaterialBuilder.BlendingMode.OPAQUE)
                .material("void material(inout MaterialInputs material) {\n" +
                        "    prepareMaterial(material);\n" +
                        "    material.baseColor = texture(materialParams_videoTexture, getUV0()).rgba;\n" +
                        "}\n")
                .build(filamentEngine);
        if (plainVideoMaterialPackage.isValid()) {
            ByteBuffer buffer = plainVideoMaterialPackage.getBuffer();
            futures.add(Material.builder()
                    .setSource(buffer)
                    .build()
                    .thenAccept(material -> {
                        plainVideoMaterial = material;
                        Log.i(TAG, "Material loaded");
                    })
                    .exceptionally(
                            throwable -> {
                                Log.e(TAG, "Unable to load material", throwable);
//                                Toast.makeText(getContext(), "Unable to load material", Toast.LENGTH_LONG).show();
                                return null;
                            }));
        }
        MaterialBuilder.shutdown();
    }

    public void onAugmentedImageTrackingUpdate(AugmentedImage augmentedImage) {
        parentFragment.onAugmentedImageUpdateListener(augmentedImage);
        AugmentedImage.TrackingMethod trackingMethod = augmentedImage.getTrackingMethod();
        TrackingState trackingState = augmentedImage.getTrackingState();
//        Log.i(TAG, "inside onAugmentedImageTrackingUpdate " + augmentedImage.getTrackingState() + " --- " + augmentedImage.getTrackingMethod());
        // If there are both images already detected, for better CPU usage we do not need scan for them

        MapDetails mapDetail = _map.get(augmentedImage.getName());
        if (mapDetail == null) {
            Log.e(TAG, "Mapdetail null. AiName: " + augmentedImage.getName());
            return;
        }
        this.getInstructionsController().setEnabled(
                InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        if (mapDetail.tracked) {
            if (!mapDetail.mediaPlayerReady || mapDetail.exoPlayer == null) {
//                Log.i(TAG, "TTT: mediaplayer fail ready:" + mapDetail.mediaPlayerReady + "  " + (mapDetail.exoPlayer == null));
                return;
            }

            if (trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING && !mapDetail.isVideoPlaying()) {
                mapDetail.startVideoPlay();
            } else if (trackingMethod != AugmentedImage.TrackingMethod.FULL_TRACKING && mapDetail.isVideoPlaying()) {
                mapDetail.pauseVideoPlay();
            }


            return;
        }

        if (trackingState == TrackingState.TRACKING && trackingMethod == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            // Setting anchor to the center of Augmented Image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
            mapDetail.tracked = true;
            Log.i(TAG, "Found image  " + augmentedImage.getName() + "   " + mapDetail.tracked);
            // AnchorNode placed to the detected tag and set it to the real size of the tag
            // This will cause deformation if your AR tag has different aspect ratio than your video
            anchorNode.setWorldScale(new Vector3(augmentedImage.getExtentX(), 1f, augmentedImage.getExtentZ()));
            getArSceneView().getScene().addChild(anchorNode);

            TransformableNode videoNode = new TransformableNode(getTransformationSystem());
            // For some reason it is shown upside down so this will rotate it correctly
            videoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), -90f));
            anchorNode.addChild(videoNode);

            // Setting texture
            ExternalTexture externalTexture = new ExternalTexture();
            RenderableInstance renderableInstance = videoNode.setRenderable(plainVideoModel.makeCopy());
            renderableInstance.setMaterial(plainVideoMaterial.makeCopy());

            // Setting MediaPLayer
            renderableInstance.getMaterial().setExternalTexture("videoTexture", externalTexture);

            mapDetail.setMediaPlayerSurface(externalTexture.getSurface());
//            mapDetail.mediaPlayer.start();
           /*
            Log.i(TAG, "Playing video in some time");
            String videoUrl = null;
            mapDetail.mediaPlayer = new MediaPlayer();
            try {
                videoUrl = mapDetail.videos.get(0).getLocalUrl();
//                mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(videoUrl));
                mapDetail.mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(videoUrl));
                mapDetail.mediaPlayer.prepareAsync();
//                    mediaPlayer.setDataSource(videoToBePlayed.getLocalUrl());
            } catch (Exception e) {

                Log.e(TAG, "cannot play localurl " + videoUrl, e);
                videoUrl = mapDetail.videos.get(0).getUrl();

                try {
                    mapDetail.mediaPlayer.setDataSource(getActivity().getApplicationContext(), Uri.parse(videoUrl));
                    mapDetail.mediaPlayer.prepareAsync();
                } catch (Exception e1) {
                    e1.printStackTrace();
                    // TODO add visual cue to user if getUrl also fails
                }

            }
//                mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(videoToBePlayed.getLocalUrl()));

//                mediaPlayer = MediaPlayer.create(getContext(), Uri.parse(videoToBePlayed.getUrl()));
            Log.i(TAG, "Playing video");
//                mediaPlayer = MediaPlayer.create(getContext(), R.raw.scan_sample);
            mapDetail.mediaPlayer.setLooping(true);
            mapDetail.mediaPlayer.setSurface(externalTexture.getSurface());
            mapDetail.mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    mapDetail.mediaPlayer.start();
                }
            });
            */
        }
//            // If rabbit model haven't been placed yet and detected image has String identifier of "rabbit"
//            // This is also example of model loading and placing at runtime
//            if (!rabbitDetected && augmentedImage.getName().equals("rabbit")) {
//                rabbitDetected = true;
//                Toast.makeText(this, "Rabbit tag detected", Toast.LENGTH_LONG).show();
//
//                anchorNode.setWorldScale(new Vector3(3.5f, 3.5f, 3.5f));
//                arFragment.getArSceneView().getScene().addChild(anchorNode);
//
//                futures.add(ModelRenderable.builder()
//                        .setSource(this, Uri.parse("models/Rabbit.glb"))
//                        .setIsFilamentGltf(true)
//                        .build()
//                        .thenAccept(rabbitModel -> {
//                            TransformableNode modelNode = new TransformableNode(arFragment.getTransformationSystem());
//                            modelNode.setRenderable(rabbitModel);
//                            anchorNode.addChild(modelNode);
//                        })
//                        .exceptionally(
//                                throwable -> {
//                                    Toast.makeText(this, "Unable to load rabbit model", Toast.LENGTH_LONG).show();
//                                    return null;
//                                }));
//            }
//        }


    }

    private void createMap() {
        for (ImageMapping imageMapping : imageMappingList) {
            ImageObj imageObj = imageMapping.getImage();
            MapDetails mapDetails = new MapDetails(imageMapping.getType(), imageMapping.getVideos());
//            ArFragmentHelper.loadMediaPlayerVideosOfMapDetails(mapDetails, MainActivity.getMainActivity());
            ArFragmentHelper.loadExoPlayerVideosOfMapDetails(mapDetails);
            _map.put(imageObj.getId(), mapDetails);
        }
    }

    @Override
    public void onCounterChanged(int count) {
        if (count >= ARUtils.getTotalImageMappingsCount()) {
            Log.i(TAG, "Callback of totalImageMappingCount recieved, reloading AiDB in ARFragment");
            reloadMappings();
            reloadAiDB();
            AugmentedImageDatabaseHelper.saveDatabase(MyApp.getContext(), ARUtils.getAugmentedImageDatabase(MainActivity.getMainActivity()));
        }
    }

    @Override
    public void onFragmentShown() {
        Log.i(TAG, "AR Fragment visible");
        reloadMappings();
        reloadAiDB();
    }

    private void reloadMappings() {
        imageMappingList = SceneFragmentHelper.getImageMappings();
        createMap();
    }

    private void reloadAiDB() {
        arConfig.setAugmentedImageDatabase(ARUtils.getAugmentedImageDatabase(MainActivity.getMainActivity()));
//        this.config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        arSession.configure(arConfig);
        setOnAugmentedImageUpdateListener(MyARFragment.this::onAugmentedImageTrackingUpdate);
    }


    private void setupARConfigs() {
        // TESTED SETTINGS
        arConfig.setFocusMode(Config.FocusMode.AUTO);


        // NOT TESTED SETTINGS
        arConfig.setDepthMode(Config.DepthMode.DISABLED);

    }
}

