package com.ps.realize.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
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
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.datamodels.ar.VideoObj;
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

public class MyARFragment extends ArFragment implements BaseArFragment.OnSessionConfigurationListener {
    private static final String TAG = MyARFragment.class.getSimpleName();
    private final List<CompletableFuture<Void>> futures = new ArrayList<>();
    private final Constants constants = new Constants();
    private final Map<String, MapDetails> _map = new HashMap<>();
    private List<ImageMapping> imageMappingList;
    private AugmentedImageDatabase database;
    private Renderable plainVideoModel;
    private Material plainVideoMaterial;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        setOnSessionConfigurationListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Bundle args = getArguments();
        if (args == null) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        String imageMappingJSONString = args.getString(constants.IMAGE_MAPPING_LIST);
        if (imageMappingJSONString != null) {
            Gson gson = new Gson();
            Type userListType = new TypeToken<ArrayList<ImageMapping>>() {
            }.getType();
            imageMappingList = gson.fromJson(imageMappingJSONString, userListType);

            createMap();
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (Sceneform.isSupported(getContext())) {
            if (imageMappingList != null) {
                Log.i(TAG, "Loading matrix and material");
                // .glb models can be loaded at runtime when needed or when app starts
                // This method loads ModelRenderable when app starts
                loadMatrixModel();
                loadMatrixMaterial();
            }


        }

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
        });
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        config.setFocusMode(Config.FocusMode.AUTO);
//        session.configure(config);

        try {
            session.resume();
            session.pause();
            session.resume();
        } catch (Exception e) {
            Log.e(TAG, "Error while resume and pausing session", e);
        }

        // Disable plane detection
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        // Images to be detected by our AR need to be added in AugmentedImageDatabase
        // This is how database is created at runtime
        // You can also prebuild database in you computer and load it directly (see: https://developers.google.com/ar/develop/java/augmented-images/guide#database)
        database = new AugmentedImageDatabase(session);

        addImagesToDatabase(session, config);


//        Bitmap matrixImage = BitmapFactory.decodeResource(getResources(), R.drawable.scan_sample_image);
//        // Every image has to have its own unique String identifier
//        database.addImage(image.getId(), matrixImage);
//
//        config.setAugmentedImageDatabase(database);
//
//        // Check for image detection
//        setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate);
    }

    private void addImagesToDatabase(Session session, Config config) {
        if (imageMappingList == null) {
            return;
        }

        Log.i(TAG, "MMM: " + imageMappingList.size());
        for (ImageMapping imageMapping : imageMappingList) {
            Log.i(TAG, "AAA: " + imageMapping);
            ImageObj imageToBeTracked = imageMapping.getImage();

            try {
                Bitmap bitmaplocal = MediaUtils.getBitmap(getContext(), Uri.parse(imageToBeTracked.getLocalUrl()));
                if (bitmaplocal != null) {
                    Bitmap targetBmp = bitmaplocal.copy(Bitmap.Config.ARGB_8888, false);
                    // Every image has to have its own unique String identifier
                    database.addImage(imageToBeTracked.getId(), targetBmp);
                    config.setAugmentedImageDatabase(database);
                    Log.i(TAG, "BBB: adding image " + imageToBeTracked.getId());
                    // Check for image detection
                    setOnAugmentedImageUpdateListener(MyARFragment.this::onAugmentedImageTrackingUpdate);
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
                                database.addImage(imageToBeTracked.getId(), matrixImage);
                                config.setAugmentedImageDatabase(database);
                                Log.i(TAG, "BBB: adding image " + imageToBeTracked.getId());
                                // Check for image detection
                                setOnAugmentedImageUpdateListener(MyARFragment.this::onAugmentedImageTrackingUpdate);
                                session.configure(config);
                                Log.i(TAG, "inside onResourceReady");
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }
                        });
            }
        }


    }

    private void loadMatrixModel() {
        Log.i(TAG, "111 Loading matrix model");
//        if (imageMapping.getType().equals(constants.VIDEO)) {
        loadVideoMatrixModel();
//        }

    }

    private void loadVideoMatrixModel() {
        Log.i(TAG, "111 Loading matrix loadVideoMatrixModel");
        futures.add(ModelRenderable.builder()
                .setSource(getContext(), Uri.parse("models/Video.glb"))
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
//        Log.i(TAG, "111 Loading matrix loadMatrixMaterial --- " + imageMapping.getType());
        Engine filamentEngine = EngineInstance.getEngine().getFilamentEngine();

//        if (imageMapping.getType().equals(constants.VIDEO)) {
        loadVideoMatrixMaterial(filamentEngine);
//        }
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
        Log.i(TAG, "inside onAugmentedImageTrackingUpdate " + augmentedImage.getTrackingState() + " --- " + augmentedImage.getTrackingMethod());
        // If there are both images already detected, for better CPU usage we do not need scan for them
        this.getInstructionsController().setEnabled(
                InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        MapDetails mapDetail = _map.get(augmentedImage.getName());
        if (mapDetail == null) {
            return;
        }

        if (mapDetail.tracked) {
            if (mapDetail.mediaPlayer != null && augmentedImage.getTrackingMethod() != AugmentedImage.TrackingMethod.FULL_TRACKING
                    && mapDetail.mediaPlayer.isPlaying()) {
                mapDetail.mediaPlayer.pause();
            } else if (mapDetail.mediaPlayer != null && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING && !mapDetail.mediaPlayer.isPlaying()) {
                mapDetail.mediaPlayer.start();
            }
            return;
        }

        if (augmentedImage.getTrackingState() == TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {
            // Setting anchor to the center of Augmented Image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));
            mapDetail.tracked = true;
            Log.i(TAG, "BBB  : " + augmentedImage.getName() + "   " + mapDetail.tracked);
            // AnchorNode placed to the detected tag and set it to the real size of the tag
            // This will cause deformation if your AR tag has different aspect ratio than your video
            anchorNode.setWorldScale(new Vector3(augmentedImage.getExtentX(), 1f, augmentedImage.getExtentZ()));
            getArSceneView().getScene().addChild(anchorNode);

            TransformableNode videoNode = new TransformableNode(getTransformationSystem());
            // For some reason it is shown upside down so this will rotate it correctly
            videoNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 180f));
            anchorNode.addChild(videoNode);

            // Setting texture
            ExternalTexture externalTexture = new ExternalTexture();
            RenderableInstance renderableInstance = videoNode.setRenderable(plainVideoModel.makeCopy());
            renderableInstance.setMaterial(plainVideoMaterial.makeCopy());

            // Setting MediaPLayer
            renderableInstance.getMaterial().setExternalTexture("videoTexture", externalTexture);

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
            _map.put(imageObj.getId(), mapDetails);
        }
    }

    class MapDetails {
        boolean tracked = false;
        String type;
        List<VideoObj> videos;

        MediaPlayer mediaPlayer;

        MapDetails(String type, List<VideoObj> videos) {
            this.type = type;
            this.videos = videos;
        }
    }
}

