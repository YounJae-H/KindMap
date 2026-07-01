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
        apply();
    }

    public void toggle() {
        config.enabled = !config.enabled;
        apply();
        saveCallback.run();
    }

    public void apply() {
        if (config.enabled) {
            captureNormalBrightness();
            brightness.setBrightness(config.enabledValue);
            return;
        }

        if (normalBrightnessCaptured) {
            brightness.setBrightness(normalBrightness);
        }
    }

    private void captureNormalBrightness() {
        if (normalBrightnessCaptured) {
            return;
        }

        normalBrightness = brightness.getBrightness();
        normalBrightnessCaptured = true;
    }
}
