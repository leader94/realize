package com.ps.realize.core.datamodels.api;

public class PostSignedLinkDM {

    String projectId;
    UploadFileDetails overlay;
    UploadFileDetails base;

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public UploadFileDetails getOverlay() {
        return overlay;
    }

    public void setOverlay(UploadFileDetails overlay) {
        this.overlay = overlay;
    }

    public UploadFileDetails getBase() {
        return base;
    }

    public void setBase(UploadFileDetails base) {
        this.base = base;
    }


}
