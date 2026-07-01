package com.younjaeh.kindmap.gamma;

public interface BrightnessAccess {
    default boolean isReady() {
        return true;
    }

    double getBrightness();

    void setBrightness(double value);
}
