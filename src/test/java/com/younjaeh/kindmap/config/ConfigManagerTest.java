package com.younjaeh.kindmap.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

final class ConfigManagerTest {
    @TempDir
    Path tempDir;

    @Test
    void createsDefaultsWhenFileIsMissing() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("kindmap.json"));

        ModConfig config = manager.load();

        assertFalse(config.gamma.enabled);
        assertEquals(1500.0, config.gamma.enabledValue);
        assertEquals("key.keyboard.g", config.gamma.toggleKey);
        assertTrue(Files.exists(tempDir.resolve("kindmap.json")));
    }

    @Test
    void clampsInvalidGammaValues() throws Exception {
        Path file = tempDir.resolve("kindmap.json");
        Files.writeString(file, """
            {
              "gamma": {
                "enabled": true,
                "enabledValue": 999999.0,
                "toggleKey": "",
                "minValue": -10.0,
                "maxValue": 1.0
              },
              "macros": []
            }
            """);
        ConfigManager manager = new ConfigManager(file);

        ModConfig config = manager.load();

        assertTrue(config.gamma.enabled);
        assertEquals(1500.0, config.gamma.enabledValue);
        assertEquals("key.keyboard.g", config.gamma.toggleKey);
        assertEquals(0.0, config.gamma.minValue);
        assertEquals(1500.0, config.gamma.maxValue);
    }

    @Test
    void savesUnicodeMacroContent() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("kindmap.json"));
        ModConfig config = ModConfig.defaults();
        MacroConfig macro = MacroConfig.defaults();
        macro.id = "ender";
        macro.name = "Ender";
        macro.key = "key.keyboard.grave.accent";
        macro.content = "/엔더";
        config.macros.add(macro);

        manager.save(config);
        ModConfig reloaded = manager.load();

        assertEquals("/엔더", reloaded.macros.getFirst().content);
    }

    @Test
    void defaultsMacroEnabledAndUsesLongSchedulingFields() throws Exception {
        MacroConfig macro = MacroConfig.defaults();

        assertTrue(macro.enabled);
        assertEquals(long.class, MacroConfig.class.getField("delayMs").getType());
        assertEquals(long.class, MacroConfig.class.getField("intervalMs").getType());
        assertEquals(0L, macro.delayMs);
        assertEquals(1000L, macro.intervalMs);
    }

    @Test
    void preservesDisabledMacroWhenSavingAndLoading() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("kindmap.json"));
        ModConfig config = ModConfig.defaults();
        MacroConfig macro = MacroConfig.defaults();
        macro.enabled = false;
        config.macros.add(macro);

        manager.save(config);
        ModConfig reloaded = manager.load();

        assertFalse(reloaded.macros.getFirst().enabled);
    }

    @Test
    void defaultsMissingMacroEnabledToTrue() throws Exception {
        Path file = tempDir.resolve("kindmap.json");
        Files.writeString(file, """
            {
              "gamma": {
                "enabled": false,
                "enabledValue": 1500.0,
                "toggleKey": "key.keyboard.g",
                "minValue": 0.0,
                "maxValue": 1500.0
              },
              "macros": [
                {
                  "id": "legacy",
                  "name": "Legacy",
                  "key": "",
                  "content": "/spawn",
                  "action": "SEND",
                  "mode": "SIMPLE",
                  "delayMs": 0,
                  "intervalMs": 1000
                }
              ]
            }
            """);
        ConfigManager manager = new ConfigManager(file);

        ModConfig config = manager.load();

        assertTrue(config.macros.getFirst().enabled);
    }

    @Test
    void clampsInvalidMacroSchedulingValues() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir.resolve("kindmap.json"));
        ModConfig config = ModConfig.defaults();
        MacroConfig macro = MacroConfig.defaults();
        macro.delayMs = -1L;
        macro.intervalMs = 1L;
        config.macros.add(macro);

        manager.save(config);
        ModConfig reloaded = manager.load();

        assertEquals(0L, reloaded.macros.getFirst().delayMs);
        assertEquals(1000L, reloaded.macros.getFirst().intervalMs);
    }

    @Test
    void recoversDefaultsFromMalformedJson() throws Exception {
        Path file = tempDir.resolve("kindmap.json");
        Files.writeString(file, "{ invalid json", java.nio.charset.StandardCharsets.UTF_8);
        ConfigManager manager = new ConfigManager(file);

        ModConfig config = manager.load();

        assertFalse(config.gamma.enabled);
        assertEquals("key.keyboard.g", config.gamma.toggleKey);
        assertTrue(config.macros.isEmpty());
        assertDoesNotThrow(() -> new ConfigManager(file).load());
    }

    @Test
    void rejectsNullConfigPath() {
        NullPointerException exception = assertThrows(
            NullPointerException.class,
            () -> new ConfigManager(null)
        );

        assertEquals("configPath", exception.getMessage());
    }
}
