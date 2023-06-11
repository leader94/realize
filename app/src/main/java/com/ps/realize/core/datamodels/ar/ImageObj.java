package com.ps.realize.core.datamodels.ar;

public class ImageObj {
    private final String id;
    private final String url;
    private final String localUrl;

    public ImageObj(String id, String url, String localUrl) {
        this.id = id;
        this.url = url;
        this.localUrl = localUrl;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getLocalUrl() {
        return localUrl;
    }
}
