package com.younjaeh.kindmap.gamma;

import com.younjaeh.kindmap.config.GammaConfig;

import java.util.Objects;

public final class GammaController {
    private final GammaConfig config;
    private final BrightnessAccess brightness;
    private final Runnable saveCallback;
    private double normalBrightness;
    private boolean normalBrightnessCaptured;

    public GammaController(GammaConfig config, BrightnessAccess brightness, Runnable saveCallback) {
        this.config = Objects.requireNonNull(config, "config");
        this.brightness = Objects.requireNonNull(brightness, "brightness");
        this.saveCallback = Objects.requireNonNull(saveCallback, "saveCallback");
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
            applyEnabledBrightness();
        } else {
            restoreNormalBrightness();
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
        brightness.setBrightness(sanitizeEnabledBrightness());
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
}
