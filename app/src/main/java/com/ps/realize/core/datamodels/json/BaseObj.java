package com.ps.realize.core.datamodels.json;

public class BaseObj {
    private final String id;
    private final String fileName;
    private final String localPath;
    private final String originalName;
    private final String originalExtn;
    private final String uploadUrl;

    public BaseObj(String id, String fileName, String localPath, String originalName, String originalExtn, String uploadUrl) {
        this.id = id;
        this.fileName = fileName;
        this.localPath = localPath;
        this.originalName = originalName;
        this.originalExtn = originalExtn;
        this.uploadUrl = uploadUrl;
    }

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getLocalPath() {
        return localPath;
    }

    public String getOriginalName() {
        return originalName;
    }

    public String getOriginalExtn() {
        return originalExtn;
    }

    public String getUploadUrl() {
        return uploadUrl;
    }
}
