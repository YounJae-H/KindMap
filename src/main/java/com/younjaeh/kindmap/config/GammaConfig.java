package com.younjaeh.kindmap.config;

public final class GammaConfig {
    public boolean enabled;
    public double enabledValue = 1500.0;
    public double normalValue = 0.5;
    public String toggleKey = "key.keyboard.g";
    public double minValue;
    public double maxValue = 1500.0;

    public static GammaConfig defaults() {
        return new GammaConfig();
    }
}
