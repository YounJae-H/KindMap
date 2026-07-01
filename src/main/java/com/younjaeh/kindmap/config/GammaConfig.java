package com.younjaeh.kindmap.config;

public final class GammaConfig {
    public boolean enabled;
    public double enabledValue;
    public String toggleKey;
    public double minValue;
    public double maxValue;

    public static GammaConfig defaults() {
        GammaConfig config = new GammaConfig();
        config.enabled = false;
        config.enabledValue = 1500.0;
        config.toggleKey = "key.keyboard.g";
        config.minValue = 0.0;
        config.maxValue = 1500.0;
        return config;
    }
}
