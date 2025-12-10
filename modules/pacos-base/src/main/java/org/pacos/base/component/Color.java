package org.pacos.base.component;

/**
 * Color definitions that match the interface
 * moved to pacos-base
 */
public enum Color {
    RED(223, 12, 0),
    GREEN(60, 150, 70),
    BLACK_LITGHT(40, 40, 40),
    LIGHTBLUE(52, 170, 220),
    YELLOW(211, 136, 0),
    ORANGE(235, 152, 0),

    TEXT("var(--lumo-body-text-color)"),
    HEADER("var(--app-header)");

    private final String rgb;

    Color(String rgb) {
        this.rgb = rgb;
    }

    Color(final int r, final int g, final int b) {
        this.rgb = r + ", " + g + ", " + b;
    }

    public String getColor() {
        return "rgb(" + rgb + ")";
    }

    public String getColorAlpha(double a) {
        return "rgba(" + rgb + "," + a + ")";
    }

}