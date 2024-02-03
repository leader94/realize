package com.ps.realize.core.datamodels.api;

import android.net.Uri;

public class UploadFileDetails {
    String fileName;
    String extn;
    Long contentLength;
    Uri localPath;

    public UploadFileDetails(String fileName, String extn, Long contentLength, Uri localPath) {
        this.fileName = fileName;
        this.extn = extn;
        this.contentLength = contentLength;
        this.localPath = localPath;
    }

    public String getFileName() {
        return fileName;
    }

    public String getExtn() {
        return extn;
    }

    public Long getContentLength() {
        return contentLength;
    }

    public Uri getLocalPath() {
        return localPath;
    }
}