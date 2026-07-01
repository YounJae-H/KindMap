package com.younjaeh.kindmap.config;

import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;

import java.util.UUID;

public final class MacroConfig {
    public boolean enabled = true;
    public String id = UUID.randomUUID().toString();
    public String name = "New Macro";
    public String key = "";
    public String content = "";
    public MacroAction action = MacroAction.SEND;
    public MacroMode mode = MacroMode.SIMPLE;
    public long delayMs;
    public long intervalMs = 1000L;

    public static MacroConfig defaults() {
        return new MacroConfig();
    }
}
