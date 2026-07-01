package com.younjaeh.kindmap.macro;

import com.younjaeh.kindmap.config.MacroConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public final class MacroManager {
    private final ChatExecutor chatExecutor;
    private List<MacroConfig> macros = List.of();
    private final Map<String, PendingDelayedMacro> delayedMacros = new HashMap<>();
    private final Map<String, Long> repeatingLastRunMs = new HashMap<>();
    private final Map<String, Long> toggledNextRunMs = new HashMap<>();

    public MacroManager(ChatExecutor chatExecutor) {
        this.chatExecutor = Objects.requireNonNull(chatExecutor, "chatExecutor");
    }

    public void setMacros(List<MacroConfig> macros) {
        this.macros = List.copyOf(macros == null ? List.of() : macros);
        clearRuntimeState();
    }

    public void onKeyPressed(String key, long nowMs, boolean typingFocused) {
        if (typingFocused || isBlank(key)) {
            return;
        }

        for (MacroConfig macro : snapshotMacros()) {
            if (!matchesKey(macro, key)) {
                continue;
            }

            switch (macro.mode) {
                case SIMPLE -> execute(macro);
                case DELAYED -> delayedMacros.put(macro.id, new PendingDelayedMacro(macro, nowMs + Math.max(0L, macro.delayMs)));
                case REPEATING -> runRepeatingMacro(macro, nowMs);
                case TOGGLE -> toggleMacro(macro, nowMs);
            }
        }
    }

    public void onKeyHeld(String key, long nowMs, boolean typingFocused) {
        if (typingFocused || isBlank(key)) {
            return;
        }

        for (MacroConfig macro : snapshotMacros()) {
            if (matchesKey(macro, key) && macro.mode == MacroMode.REPEATING) {
                runRepeatingMacro(macro, nowMs);
            }
        }
    }

    public void tick(long nowMs) {
        runDelayedMacros(nowMs);
        runToggledMacros(nowMs);
    }

    public void clearRuntimeState() {
        delayedMacros.clear();
        repeatingLastRunMs.clear();
        toggledNextRunMs.clear();
    }

    private void runDelayedMacros(long nowMs) {
        Set<String> dueIds = new HashSet<>();
        for (Map.Entry<String, PendingDelayedMacro> entry : new ArrayList<>(delayedMacros.entrySet())) {
            PendingDelayedMacro pending = entry.getValue();
            if (nowMs >= pending.dueAtMs() && isRunnable(pending.macro())) {
                execute(pending.macro());
                dueIds.add(entry.getKey());
            }
        }
        dueIds.forEach(delayedMacros::remove);
    }

    private void runToggledMacros(long nowMs) {
        for (MacroConfig macro : snapshotMacros()) {
            if (!isRunnable(macro) || macro.mode != MacroMode.TOGGLE) {
                continue;
            }
            Long nextRunMs = toggledNextRunMs.get(macro.id);
            if (nextRunMs != null && nowMs >= nextRunMs) {
                execute(macro);
                toggledNextRunMs.put(macro.id, nowMs + intervalMs(macro));
            }
        }
    }

    private void runRepeatingMacro(MacroConfig macro, long nowMs) {
        Long lastRunMs = repeatingLastRunMs.get(macro.id);
        if (lastRunMs == null || nowMs - lastRunMs >= intervalMs(macro)) {
            execute(macro);
            repeatingLastRunMs.put(macro.id, nowMs);
        }
    }

    private void toggleMacro(MacroConfig macro, long nowMs) {
        if (toggledNextRunMs.containsKey(macro.id)) {
            toggledNextRunMs.remove(macro.id);
        } else {
            toggledNextRunMs.put(macro.id, nowMs);
        }
    }

    private void execute(MacroConfig macro) {
        if (macro.action == MacroAction.TYPE) {
            chatExecutor.type(macro.content);
        } else {
            chatExecutor.send(macro.content);
        }
    }

    private List<MacroConfig> snapshotMacros() {
        return List.copyOf(macros);
    }

    private static boolean matchesKey(MacroConfig macro, String key) {
        return isRunnable(macro) && key.equals(macro.key);
    }

    private static boolean isRunnable(MacroConfig macro) {
        return macro != null
                && macro.enabled
                && !isBlank(macro.id)
                && !isBlank(macro.key)
                && !isBlank(macro.content)
                && macro.mode != null;
    }

    private static long intervalMs(MacroConfig macro) {
        return Math.max(50L, macro.intervalMs);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record PendingDelayedMacro(MacroConfig macro, long dueAtMs) {
    }
}
