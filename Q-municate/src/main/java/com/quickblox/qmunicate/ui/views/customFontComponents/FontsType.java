package com.quickblox.qmunicate.ui.views.customFontComponents;

public enum FontsType {

    LIGHT("fonts/Roboto-Light.ttf"),
    NORMAL("fonts/Roboto-Regular.ttf"),
    BOLD("fonts/Roboto-Bold.ttf"),
    ITALIC("fonts/Roboto-Italic.ttf"),
    BOLD_ITALIC("fonts/Roboto-BoldItalic.ttf");

    private String path;

    private FontsType(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}