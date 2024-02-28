package com.ps.realize.core.helperClasses;

import android.util.Log;

import com.ps.realize.core.data.LocalData;
import com.ps.realize.core.datamodels.ar.ImageMapping;
import com.ps.realize.core.datamodels.ar.ImageObj;
import com.ps.realize.core.datamodels.ar.VideoObj;
import com.ps.realize.core.datamodels.json.BaseObj;
import com.ps.realize.core.datamodels.json.OverlayObj;
import com.ps.realize.core.datamodels.json.ProjectObj;
import com.ps.realize.core.datamodels.json.SceneObj;
import com.ps.realize.utils.ARUtils;
import com.ps.realize.utils.Constants;

import java.util.ArrayList;
import java.util.List;

public class SceneFragmentHelper {
    private static final String TAG = SceneFragmentHelper.class.getSimpleName();

    public static List<ImageMapping> getImageMappings() {

        List<ImageMapping> imageMappingList = new ArrayList();
        ProjectObj project = LocalData.curProject;

        try {
            List<SceneObj> scenes = project.getScenes();
            int count = 0;
//            scenes.forEach((scene) -> {

            for (int i = 0; i < scenes.size(); i++) {
                SceneObj scene = scenes.get(i);

                OverlayObj overlayObj = scene.getOverlays().get(0);
                BaseObj baseObj = scene.getBases().get(0);
                ImageObj image = new ImageObj(baseObj.getId(),
                        baseObj.getUploadUrl() + "/" + baseObj.getFileName(),
                        baseObj.getLocalPath());
                VideoObj video = new VideoObj(overlayObj.getId(),
                        overlayObj.getUploadUrl() + "/" + overlayObj.getFileName(),
                        overlayObj.getLocalPath());
                List<VideoObj> videoList = new ArrayList<>();
                videoList.add(video);
                ImageMapping imageMapping = new ImageMapping(videoList, image);
                if (count < Constants.MAX_SCENE_COUNT) {
                    imageMappingList.add(imageMapping);
                    count++;
                } else {
                    break;
                }


                Log.i(TAG, "BBB: adding image " + image.getUrl());
            }
//            });

//            SceneObj scene = scenes.get(0);

        } catch (Exception e) {
            Log.e(TAG, " Failed getting ImageMappings ", e);
        }
        ARUtils.setTotalImageMappingsCount(imageMappingList.size());
        return imageMappingList;
    }

    // TODO optimise this to delete directly instead of looping
    public static boolean deleteSceneObject(String id) {
        ProjectObj project = LocalData.curProject;
        boolean deleted = false;

        List<SceneObj> scenes = project.getScenes();


        for (int i = 0; i < scenes.size(); i++) {
            SceneObj scene = scenes.get(i);

            BaseObj baseObj = scene.getBases().get(0);

            if (baseObj.getId() == id) {
                scenes.remove(i);
                deleted = true;
                break;
            }
        }

        return deleted;
    }
}
