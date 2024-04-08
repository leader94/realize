package com.ps.realize.ui.bottomSheet;

public class ItemTextBottomSheetItem {
    ICON_TEXT_BTMSHEET id;
    int icon;
    String text;

    public ItemTextBottomSheetItem(ICON_TEXT_BTMSHEET id, int icon, String text) {
        this.id = id;
        this.icon = icon;
        this.text = text;
    }

    public ICON_TEXT_BTMSHEET getId() {
        return id;
    }

    public void setId(ICON_TEXT_BTMSHEET id) {
        this.id = id;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public enum ICON_TEXT_BTMSHEET {
        CAMERA,
        LOCAL,
        LINK,
        YOUTUBE

    }
}
