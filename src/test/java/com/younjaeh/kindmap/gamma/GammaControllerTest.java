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

        assertEquals(15.0, brightness.current);
        assertEquals(0, saveCounter.count);
    }

    @Test
    void enabledStartupDefersUntilBrightnessOptionsAreReady() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 1500.0;
        FakeBrightness brightness = new FakeBrightness(0.5);
        brightness.ready = false;
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        assertFalse(controller.initialize());
        assertEquals(0.5, brightness.current);

        brightness.ready = true;

        assertTrue(controller.initialize());
        assertEquals(15.0, brightness.current);
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
        assertEquals(15.0, brightness.current);
        assertEquals(1, saveCounter.count);
    }

    @Test
    void togglingOffRestoresNormalBrightnessAndPersists() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.normalValue = 0.5;
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
    void startupEnabledUsesPersistedNormalBrightnessWhenCurrentBrightnessIsBoosted() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 1500.0;
        config.normalValue = 0.5;
        FakeBrightness brightness = new FakeBrightness(1500.0);
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        controller.initialize();
        controller.toggle();

        assertFalse(config.enabled);
        assertEquals(0.5, brightness.current);
    }

    @Test
    void togglingOnStoresNormalBrightnessForFutureRestores() {
        GammaConfig config = GammaConfig.defaults();
        FakeBrightness brightness = new FakeBrightness(0.8);
        RecordingNotifier notifier = new RecordingNotifier();
        GammaController controller = new GammaController(config, brightness, () -> {
        }, notifier);

        controller.toggle();

        assertTrue(config.enabled);
        assertEquals(0.8, config.normalValue);
        assertEquals(15.0, brightness.current);
        assertEquals(1500, notifier.lastPercent);
    }

    @Test
    void togglingOffShowsRestoredNormalBrightnessPercent() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.normalValue = 0.8;
        FakeBrightness brightness = new FakeBrightness(15.0);
        RecordingNotifier notifier = new RecordingNotifier();
        GammaController controller = new GammaController(config, brightness, () -> {
        }, notifier);

        controller.initialize();
        controller.toggle();

        assertFalse(config.enabled);
        assertEquals(0.8, brightness.current);
        assertEquals(80, notifier.lastPercent);
    }

    @Test
    void applyingSettingsEnableCapturesCurrentNormalBrightness() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = false;
        config.normalValue = 0.2;
        FakeBrightness brightness = new FakeBrightness(0.8);
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        config.enabled = true;
        controller.applyAfterConfigEdit(false);
        config.enabled = false;
        controller.applyAfterConfigEdit(true);

        assertEquals(0.8, brightness.current);
        assertEquals(0.8, config.normalValue);
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

        assertEquals(1.0, brightness.current);
    }

    @Test
    void clampsRuntimeEnabledValueBeforeApplying() {
        GammaConfig config = GammaConfig.defaults();
        config.enabled = true;
        config.enabledValue = 999.0;
        config.minValue = 0.0;
        config.maxValue = 100.0;
        FakeBrightness brightness = new FakeBrightness(0.5);
        GammaController controller = new GammaController(config, brightness, () -> {
        });

        controller.initialize();

        assertEquals(1.0, brightness.current);
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
        private boolean ready = true;

        private FakeBrightness(double current) {
            this.current = current;
        }

        @Override
        public boolean isReady() {
            return ready;
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

    private static final class RecordingNotifier implements GammaNotifier {
        private int lastPercent = -1;

        @Override
        public void showGammaPercent(int percent) {
            lastPercent = percent;
        }
    }
}
