package com.ps.realize.core.datamodels.api;

import com.ps.realize.core.datamodels.internal.VideoItem;

import java.util.List;

public class YoutubeSearchDM {
    List<VideoItem> items;
    int size;

    public List<VideoItem> getItems() {
        return items;
    }

    public void setItems(List<VideoItem> items) {
        this.items = items;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
