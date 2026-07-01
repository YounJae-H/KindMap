package com.younjaeh.kindmap.ui;

import com.younjaeh.kindmap.config.GammaConfig;
import com.younjaeh.kindmap.config.MacroConfig;
import com.younjaeh.kindmap.config.ModConfig;
import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;

import java.util.ArrayList;
import java.util.Objects;

public final class KindMapConfigDraft {
    private KindMapConfigDraft() {
    }

    public static ModConfig copyOf(ModConfig source) {
        ModConfig safeSource = source == null ? ModConfig.defaults() : source;
        ModConfig copy = ModConfig.defaults();
        copy.gamma = copyGamma(safeSource.gamma);
        copy.macros = new ArrayList<>();
        if (safeSource.macros != null) {
            for (MacroConfig macro : safeSource.macros) {
                if (macro != null) {
                    copy.macros.add(copyMacro(macro));
                }
            }
        }
        return copy;
    }

    public static void applyTo(ModConfig target, ModConfig draft) {
        Objects.requireNonNull(target, "target");
        ModConfig safeDraft = copyOf(draft);
        if (target.gamma == null) {
            target.gamma = GammaConfig.defaults();
        }
        copyGammaInto(target.gamma, safeDraft.gamma);
        target.macros = new ArrayList<>();
        for (MacroConfig macro : safeDraft.macros) {
            target.macros.add(copyMacro(macro));
        }
    }

    public static double parseDouble(String value, double fallback, double min, double max) {
        double parsed;
        try {
            parsed = Double.parseDouble(value == null ? "" : value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
        if (!Double.isFinite(parsed)) {
            return fallback;
        }
        if (parsed < min) {
            return min;
        }
        if (parsed > max) {
            return max;
        }
        return parsed;
    }

    public static long parseLong(String value, long fallback, long min) {
        long parsed;
        try {
            parsed = Long.parseLong(value == null ? "" : value.trim());
        } catch (NumberFormatException exception) {
            return fallback;
        }
        return Math.max(min, parsed);
    }

    static MacroConfig newMacro() {
        return MacroConfig.defaults();
    }

    private static GammaConfig copyGamma(GammaConfig source) {
        GammaConfig copy = GammaConfig.defaults();
        if (source != null) {
            copyGammaInto(copy, source);
        }
        return copy;
    }

    private static void copyGammaInto(GammaConfig target, GammaConfig source) {
        target.enabled = source.enabled;
        target.enabledValue = source.enabledValue;
        target.normalValue = source.normalValue;
        target.toggleKey = source.toggleKey;
        target.minValue = source.minValue;
        target.maxValue = source.maxValue;
    }

    private static MacroConfig copyMacro(MacroConfig source) {
        MacroConfig copy = MacroConfig.defaults();
        copy.enabled = source.enabled;
        copy.id = source.id;
        copy.name = source.name;
        copy.key = source.key;
        copy.content = source.content;
        copy.action = source.action == null ? MacroAction.SEND : source.action;
        copy.mode = source.mode == null ? MacroMode.SIMPLE : source.mode;
        copy.delayMs = source.delayMs;
        copy.intervalMs = source.intervalMs;
        return copy;
    }
}
