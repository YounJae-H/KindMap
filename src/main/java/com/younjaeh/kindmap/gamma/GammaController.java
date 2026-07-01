package com.younjaeh.kindmap.gamma;

import com.younjaeh.kindmap.config.GammaConfig;

import java.util.Objects;

public final class GammaController {
    private final GammaConfig config;
    private final BrightnessAccess brightness;
    private final Runnable saveCallback;
    private final GammaNotifier notifier;
    private double normalBrightness;
    private boolean normalBrightnessCaptured;

    public GammaController(GammaConfig config, BrightnessAccess brightness, Runnable saveCallback) {
        this(config, brightness, saveCallback, GammaNotifier.NONE);
    }

    public GammaController(GammaConfig config, BrightnessAccess brightness, Runnable saveCallback, GammaNotifier notifier) {
        this.config = Objects.requireNonNull(config, "config");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        this.saveCallback = Objects.requireNonNull(saveCallback, "saveCallback");
        this.notifier = Objects.requireNonNull(notifier, "notifier");
    }

    public void initialize() {
        if (config.enabled) {
            loadPersistedNormalBrightness();
            applyEnabledBrightness();
        }
    }

    public void toggle() {
        config.enabled = !config.enabled;
        if (config.enabled) {
            captureCurrentNormalBrightness();
            double enabledPercent = sanitizeEnabledBrightness();
            applyEnabledBrightness(enabledPercent);
            notifier.showGammaPercent(toDisplayPercent(enabledPercent));
        } else {
            double restoredBrightness = normalBrightnessCaptured ? normalBrightness : sanitizeNormalBrightness(config.normalValue);
            restoreNormalBrightness();
            notifier.showGammaPercent(rawBrightnessToDisplayPercent(restoredBrightness));
        }
        saveCallback.run();
    }

    public void apply() {
        if (config.enabled) {
            if (!normalBrightnessCaptured) {
                loadPersistedNormalBrightness();
            }
            applyEnabledBrightness();
            return;
        }

        restoreNormalBrightness();
    }

    public void applyAfterConfigEdit(boolean wasEnabled) {
        if (config.enabled) {
            if (!wasEnabled) {
                captureCurrentNormalBrightness();
            } else if (!normalBrightnessCaptured) {
                loadPersistedNormalBrightness();
            }
            applyEnabledBrightness();
            return;
        }

        restoreNormalBrightness();
    }

    private void restoreNormalBrightness() {
        if (normalBrightnessCaptured) {
            brightness.setBrightness(normalBrightness);
            config.normalValue = normalBrightness;
            normalBrightnessCaptured = false;
        }
    }

    private void captureCurrentNormalBrightness() {
        normalBrightness = brightness.getBrightness();
        config.normalValue = normalBrightness;
        normalBrightnessCaptured = true;
    }

    private void loadPersistedNormalBrightness() {
        normalBrightness = sanitizeNormalBrightness(config.normalValue);
        config.normalValue = normalBrightness;
        normalBrightnessCaptured = true;
    }

    private void applyEnabledBrightness() {
        applyEnabledBrightness(sanitizeEnabledBrightness());
    }

    private void applyEnabledBrightness(double enabledPercent) {
        brightness.setBrightness(GammaSliderRange.rawBrightnessFromPercent(enabledPercent));
    }

    private double sanitizeEnabledBrightness() {
        double minValue = config.minValue;
        if (!Double.isFinite(minValue) || minValue < 0.0) {
            minValue = 0.0;
        }

        double maxValue = config.maxValue;
        if (!Double.isFinite(maxValue) || maxValue < 2.0 || maxValue < minValue) {
            minValue = 0.0;
            maxValue = 1500.0;
        }

        double enabledValue = config.enabledValue;
        if (!Double.isFinite(enabledValue)) {
            return maxValue;
        }
        if (enabledValue < minValue) {
            return minValue;
        }
        if (enabledValue > maxValue) {
            return maxValue;
        }
        return enabledValue;
    }

    private static double sanitizeNormalBrightness(double value) {
        if (!Double.isFinite(value) || value < 0.0) {
            return 0.5;
        }
        return value;
    }

    private static int toDisplayPercent(double percent) {
        return (int) Math.round(percent);
    }

    private static int rawBrightnessToDisplayPercent(double value) {
        return GammaSliderRange.displayPercentFromRawBrightness(value);
    }
}
