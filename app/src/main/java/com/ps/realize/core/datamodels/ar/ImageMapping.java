package com.ps.realize.core.datamodels.ar;

import com.ps.realize.utils.Constants;

import java.util.List;

public class ImageMapping {
    private final List<VideoObj> videos;
    private final String type;
    private final ImageObj image;
    private final Constants constants = new Constants();

    public ImageMapping(List<VideoObj> video, ImageObj image) {
        this.videos = video;
        this.image = image;
        this.type = constants.VIDEO;
    }

    public ImageMapping(List<VideoObj> video, ImageObj image, String type) {
        this.videos = video;
        this.image = image;
        this.type = type;
    }


    public List getVideos() {
        return videos;
    }

    public String getType() {
        return type;
    }

    public ImageObj getImage() {
        return image;
    }


}
