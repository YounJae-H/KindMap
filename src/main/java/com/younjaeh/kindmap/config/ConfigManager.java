package com.younjaeh.kindmap.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public final class ConfigManager {
    private static final long MIN_MACRO_INTERVAL_MS = 50L;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Path configPath;

    public ConfigManager(Path configPath) {
        this.configPath = Objects.requireNonNull(configPath, "configPath");
    }

    public ModConfig load() throws IOException {
        if (Files.notExists(configPath)) {
            ModConfig defaults = ModConfig.defaults();
            save(defaults);
            return defaults;
        }

        ModConfig config;
        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            config = GSON.fromJson(reader, ModConfig.class);
        } catch (JsonParseException exception) {
            ModConfig defaults = ModConfig.defaults();
            save(defaults);
            return defaults;
        }

        ModConfig validated = validate(config);
        save(validated);
        return validated;
    }

    public void save(ModConfig config) throws IOException {
        ModConfig validated = validate(config);
        Path parent = configPath.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        try (Writer writer = Files.newBufferedWriter(configPath, StandardCharsets.UTF_8)) {
            GSON.toJson(validated, writer);
        }
    }

    static ModConfig validate(ModConfig config) {
        ModConfig validated = config == null ? ModConfig.defaults() : config;
        validated.gamma = validateGamma(validated.gamma);
        validated.macros = validateMacros(validated.macros);
        return validated;
    }

    private static GammaConfig validateGamma(GammaConfig gamma) {
        GammaConfig validated = gamma == null ? GammaConfig.defaults() : gamma;
        if (!Double.isFinite(validated.minValue) || validated.minValue < 0.0) {
            validated.minValue = 0.0;
        }
        if (!Double.isFinite(validated.maxValue) || validated.maxValue < 2.0 || validated.maxValue < validated.minValue) {
            validated.minValue = 0.0;
            validated.maxValue = 1500.0;
        }
        if (!Double.isFinite(validated.enabledValue)) {
            validated.enabledValue = validated.maxValue;
        }
        if (validated.enabledValue < validated.minValue) {
            validated.enabledValue = validated.minValue;
        }
        if (validated.enabledValue > validated.maxValue) {
            validated.enabledValue = validated.maxValue;
        }
        if (!Double.isFinite(validated.normalValue) || validated.normalValue < 0.0) {
            validated.normalValue = 0.5;
        }
        if (isBlank(validated.toggleKey)) {
            validated.toggleKey = "key.keyboard.g";
        }
        return validated;
    }

    private static ArrayList<MacroConfig> validateMacros(Iterable<MacroConfig> macros) {
        ArrayList<MacroConfig> validated = new ArrayList<>();
        Set<String> seenIds = new HashSet<>();
        if (macros == null) {
            return validated;
        }

        for (MacroConfig macro : macros) {
            if (macro == null) {
                continue;
            }
            validated.add(validateMacro(macro));
            if (isBlank(macro.id) || !seenIds.add(macro.id)) {
                macro.id = UUID.randomUUID().toString();
                seenIds.add(macro.id);
            }
        }
        return validated;
    }

    private static MacroConfig validateMacro(MacroConfig macro) {
        if (isBlank(macro.id)) {
            macro.id = UUID.randomUUID().toString();
        }
        if (isBlank(macro.name)) {
            macro.name = "New Macro";
        }
        if (macro.key == null) {
            macro.key = "";
        }
        if (macro.content == null) {
            macro.content = "";
        }
        if (macro.action == null) {
            macro.action = MacroAction.SEND;
        }
        if (macro.mode == null) {
            macro.mode = MacroMode.SIMPLE;
        }
        if (macro.delayMs < 0) {
            macro.delayMs = 0L;
        }
        if (macro.intervalMs < MIN_MACRO_INTERVAL_MS) {
            macro.intervalMs = MIN_MACRO_INTERVAL_MS;
        }
        return macro;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
