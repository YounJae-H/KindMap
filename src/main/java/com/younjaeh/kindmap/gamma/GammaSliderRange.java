package com.younjaeh.kindmap.gamma;

public final class GammaSliderRange {
    private static final double FALLBACK_MAX_PERCENT = 1500.0;

    private GammaSliderRange() {
    }

    public static double rawBrightnessFromPercent(double percent) {
        return percent / 100.0;
    }

    public static double percentFromRawBrightness(double rawBrightness) {
        return rawBrightness * 100.0;
    }

    public static int displayPercentFromRawBrightness(double rawBrightness) {
        return (int) Math.round(percentFromRawBrightness(rawBrightness));
    }

    public static double sliderValueFromRawBrightness(double rawBrightness, double maxPercent) {
        double maxRawBrightness = rawBrightnessFromPercent(sanitizeMaxPercent(maxPercent));
        if (!Double.isFinite(rawBrightness) || rawBrightness <= 0.0) {
            return 0.0;
        }
        return clamp(rawBrightness / maxRawBrightness, 0.0, 1.0);
    }

    public static double rawBrightnessFromSliderValue(double sliderValue, double maxPercent) {
        double maxRawBrightness = rawBrightnessFromPercent(sanitizeMaxPercent(maxPercent));
        if (!Double.isFinite(sliderValue)) {
            return maxRawBrightness;
        }
        return clamp(sliderValue, 0.0, 1.0) * maxRawBrightness;
    }

    public static double sanitizeMaxPercent(double maxPercent) {
        if (!Double.isFinite(maxPercent) || maxPercent <= 0.0) {
            return FALLBACK_MAX_PERCENT;
        }
        return maxPercent;
    }

    private static double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
