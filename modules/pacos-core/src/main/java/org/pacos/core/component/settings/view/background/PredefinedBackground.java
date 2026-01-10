package org.pacos.core.component.settings.view.background;

public enum PredefinedBackground {

    ONE("img/wallpaper/1.jpeg", Constants.X_1080),
    TWO("img/wallpaper/2.jpeg", Constants.X_1080),
    THREE("img/wallpaper/3.jpeg", Constants.X_1080),
    FOUR("img/wallpaper/4.jpeg", Constants.X_1080),
    FIVE("img/wallpaper/5.jpeg", Constants.X_1080),
    SIX("img/wallpaper/6.jpeg", Constants.X_1080),
    SEVEN("img/wallpaper/7.jpeg", Constants.X_1700),
    EIGHT("img/wallpaper/8.jpeg", Constants.X_1700),
    NINE("img/wallpaper/9.jpeg", Constants.X_1700),
    ;

    private final String src;
    private final String resolution;

    PredefinedBackground(String src, String title) {
        this.src = src;
        this.resolution = title;
    }

    public String getSrc() {
        return src;
    }

    public String getResolution() {
        return resolution;
    }

    private static class Constants {
        public static final String X_1080 = "1920x1080";
        public static final String X_1700 = "2560x1700";
    }
}
