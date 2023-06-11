package com.ps.realize.core.datamodels.json;

import java.util.List;

public class SceneObj {
    String id;
    List<OverlayObj> overlays;
    List<BaseObj> bases;

    public SceneObj(String id, List<OverlayObj> overlay, List<BaseObj> base) {
        this.id = id;
        this.overlays = overlay;
        this.bases = base;
    }

    public String getId() {
        return id;
    }

    public List<OverlayObj> getOverlays() {
        return overlays;
    }

    public List<BaseObj> getBases() {
        return bases;
    }
}
