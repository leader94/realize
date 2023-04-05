package com.ps.realize.core;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.filament.filamat.MaterialBuilder;
import com.google.android.filament.Engine;
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
import com.ps.realize.R;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;


public class MyARFragment extends ArFragment implements   BaseArFragment.OnSessionConfigurationListener{
    private static final String TAG = MyARFragment.class.getSimpleName();
    SceneFragment sceneFragment;

    private final List<CompletableFuture<Void>> futures = new ArrayList<>();
    private AugmentedImageDatabase database;
    private  boolean imageDetected = false;
    private Renderable plainVideoModel;
    private Material plainVideoMaterial;
    private MediaPlayer mediaPlayer;

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
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(Sceneform.isSupported(getContext())) {
            // .glb models can be loaded at runtime when needed or when app starts
            // This method loads ModelRenderable when app starts
            loadMatrixModel();
            loadMatrixMaterial();
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

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        }
    }

    @Override
    public void onSessionConfiguration(Session session, Config config) {
        // Disable plane detection
        config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);

        // Images to be detected by our AR need to be added in AugmentedImageDatabase
        // This is how database is created at runtime
        // You can also prebuild database in you computer and load it directly (see: https://developers.google.com/ar/develop/java/augmented-images/guide#database)

        database = new AugmentedImageDatabase(session);

        Bitmap matrixImage = BitmapFactory.decodeResource(getResources(), R.drawable.scan_sample_image);
        // Every image has to have its own unique String identifier
        database.addImage("image_tag", matrixImage);

        config.setAugmentedImageDatabase(database);

        // Check for image detection
        setOnAugmentedImageUpdateListener(this::onAugmentedImageTrackingUpdate);
    }

    private void loadMatrixModel() {
        futures.add(ModelRenderable.builder()
                .setSource(getContext(), Uri.parse("models/Video.glb"))
                .setIsFilamentGltf(true)
                .build()
                .thenAccept(model -> {
                    //removing shadows for this Renderable
                    model.setShadowCaster(false);
                    model.setShadowReceiver(true);
                    plainVideoModel = model;
                })
                .exceptionally(
                        throwable -> {
                            Toast.makeText(getContext(), "Unable to load renderable", Toast.LENGTH_LONG).show();
                            return null;
                        }));
    }

    private void loadMatrixMaterial() {
        Engine filamentEngine = EngineInstance.getEngine().getFilamentEngine();

        MaterialBuilder.init();
        MaterialBuilder materialBuilder = new MaterialBuilder()
                .platform(MaterialBuilder.Platform.MOBILE)
                .name("External Video Material")
                .require(MaterialBuilder.VertexAttribute.UV0)
                .shading(MaterialBuilder.Shading.UNLIT)
                .doubleSided(true)
                .samplerParameter(MaterialBuilder.SamplerType.SAMPLER_EXTERNAL, MaterialBuilder.SamplerFormat.FLOAT, MaterialBuilder.ParameterPrecision.DEFAULT, "videoTexture")
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
                    })
                    .exceptionally(
                            throwable -> {
                                Toast.makeText(getContext(), "Unable to load material", Toast.LENGTH_LONG).show();
                                return null;
                            }));
        }
        MaterialBuilder.shutdown();
    }
    public void onAugmentedImageTrackingUpdate(AugmentedImage augmentedImage) {
        // If there are both images already detected, for better CPU usage we do not need scan for them
        if (imageDetected) {
            return;
        }

        if (augmentedImage.getTrackingState() == TrackingState.TRACKING
                && augmentedImage.getTrackingMethod() == AugmentedImage.TrackingMethod.FULL_TRACKING) {

            // Setting anchor to the center of Augmented Image
            AnchorNode anchorNode = new AnchorNode(augmentedImage.createAnchor(augmentedImage.getCenterPose()));

            // If matrix video haven't been placed yet and detected image has String identifier of "matrix"
            if (!imageDetected && augmentedImage.getName().equals("image_tag")) {
                imageDetected = true;
                Toast.makeText(getContext(), "Matrix tag detected", Toast.LENGTH_LONG).show();

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
                RenderableInstance renderableInstance = videoNode.setRenderable(plainVideoModel);
                renderableInstance.setMaterial(plainVideoMaterial);

                // Setting MediaPLayer
                renderableInstance.getMaterial().setExternalTexture("videoTexture", externalTexture);
                mediaPlayer = MediaPlayer.create(getContext(), R.raw.scan_sample);
                mediaPlayer.setLooping(true);
                mediaPlayer.setSurface(externalTexture.getSurface());
                mediaPlayer.start();
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
        }
        if (imageDetected) {
            this.getInstructionsController().setEnabled(
                    InstructionsController.TYPE_AUGMENTED_IMAGE_SCAN, false);
        }
    }
}
