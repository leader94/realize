package com.ps.realize.core.datamodels.json;

import java.util.List;

public class ProjectObj {
    private final List<SceneObj> scenes;
    private String id;

    public ProjectObj(String projectId, List<SceneObj> scenes) {
        this.id = projectId;
        this.scenes = scenes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<SceneObj> getScenes() {
        return scenes;
    }
}
