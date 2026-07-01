package com.younjaeh.kindmap.gamma;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class GammaSliderRangeTest {
    @Test
    void mapsBoostedGammaToNormalizedSliderPosition() {
        assertEquals(1.0, GammaSliderRange.sliderValueFromRawBrightness(15.0, 1500.0));
    }

    @Test
    void mapsNormalizedSliderPositionBackToBoostedGamma() {
        assertEquals(15.0, GammaSliderRange.rawBrightnessFromSliderValue(1.0, 1500.0));
        assertEquals(7.5, GammaSliderRange.rawBrightnessFromSliderValue(0.5, 1500.0));
    }

    @Test
    void displaysRawGammaAsPercent() {
        assertEquals(1500, GammaSliderRange.displayPercentFromRawBrightness(15.0));
        assertEquals(80, GammaSliderRange.displayPercentFromRawBrightness(0.8));
    }

    @Test
    void clampsSliderValuesToUsableRange() {
        assertEquals(0.0, GammaSliderRange.rawBrightnessFromSliderValue(-1.0, 1500.0));
        assertEquals(15.0, GammaSliderRange.rawBrightnessFromSliderValue(2.0, 1500.0));
        assertEquals(1.0, GammaSliderRange.sliderValueFromRawBrightness(30.0, 1500.0));
    }
}
