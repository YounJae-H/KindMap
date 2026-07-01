package com.younjaeh.kindmap.gamma;

import com.younjaeh.kindmap.config.GammaConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class GammaControllerTest {
    @Test
    void appliesSavedEnabledStateOnStartup() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 1500.0;
        FakeBrightness brightness = new FakeBrightness(0.5);
        SaveCounter saveCounter = new SaveCounter();
        GammaController controller = new GammaController(config, brightness, saveCounter);

        controller.initialize();

        assertEquals(1500.0, brightness.current);
        assertEquals(0, saveCounter.count);
    }

    @Test
    void togglingOnPersistsState() {
        GammaConfig config = GammaConfig.defaults();
        FakeBrightness brightness = new FakeBrightness(0.5);
        SaveCounter saveCounter = new SaveCounter();
        GammaController controller = new GammaController(config, brightness, saveCounter);

        controller.initialize();
        controller.toggle();

        assertTrue(config.enabled);
        assertEquals(1500.0, brightness.current);
        assertEquals(1, saveCounter.count);
    }

    @Test
    void togglingOffRestoresNormalBrightnessAndPersists() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        FakeBrightness brightness = new FakeBrightness(0.5);
        SaveCounter saveCounter = new SaveCounter();
        GammaController controller = new GammaController(config, brightness, saveCounter);

        controller.initialize();
        controller.toggle();

        assertFalse(config.enabled);
        assertEquals(0.5, brightness.current);
        assertEquals(1, saveCounter.count);
    }

    @Test
    void togglingAfterNormalBrightnessChangeRestoresLatestNormalBrightness() {
        GammaConfig config = GammaConfig.defaults();
        FakeBrightness brightness = new FakeBrightness(0.5);
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        controller.toggle();
        controller.toggle();
        brightness.current = 0.8;
        controller.toggle();
        controller.toggle();

        assertEquals(0.8, brightness.current);
    }

    @Test
    void appliesCurrentConfigEnabledValue() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 100.0;
        FakeBrightness brightness = new FakeBrightness(0.5);
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        controller.initialize();

        assertEquals(100.0, brightness.current);
    }

    @Test
    void initializeWhenOffLeavesBrightnessUnchangedAndDoesNotSave() {
        GammaConfig config = GammaConfig.defaults();
        FakeBrightness brightness = new FakeBrightness(0.5);
        SaveCounter saveCounter = new SaveCounter();
        GammaController controller = new GammaController(config, brightness, saveCounter);

        controller.initialize();

        assertFalse(config.enabled);
        assertEquals(0.5, brightness.current);
        assertEquals(0, saveCounter.count);
    }

    private static final class FakeBrightness implements BrightnessAccess {
        private double current;

        private FakeBrightness(double current) {
            this.current = current;
        }

        @Override
        public double getBrightness() {
            return current;
        }

        @Override
        public void setBrightness(double value) {
            current = value;
        }
    }

    private static final class SaveCounter implements Runnable {
        private int count;

        @Override
        public void run() {
            count++;
        }
    }
}
