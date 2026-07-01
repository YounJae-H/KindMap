package com.younjaeh.kindmap.config;

import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;

public final class MacroConfig {
    public boolean enabled;
    public String id;
    public String name;
    public String key;
    public String content;
    public MacroAction action;
    public MacroMode mode;
    public long delayMs;
    public long intervalMs;

    public static MacroConfig defaults() {
        MacroConfig config = new MacroConfig();
        config.enabled = true;
        config.id = "";
        config.name = "New Macro";
        config.key = "";
        config.content = "";
        config.action = MacroAction.SEND;
        config.mode = MacroMode.SIMPLE;
        config.delayMs = 0L;
        config.intervalMs = 1000L;
        return config;
    }
}
