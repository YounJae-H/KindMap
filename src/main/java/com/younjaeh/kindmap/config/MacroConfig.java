package com.younjaeh.kindmap.config;

import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;

public final class MacroConfig {
    public String id;
    public String name;
    public String key;
    public String content;
    public MacroAction action;
    public MacroMode mode;
    public int delayMs;
    public int intervalMs;

    public static MacroConfig defaults() {
        MacroConfig config = new MacroConfig();
        config.id = "";
        config.name = "New Macro";
        config.key = "";
        config.content = "";
        config.action = MacroAction.SEND;
        config.mode = MacroMode.SIMPLE;
        config.delayMs = 0;
        config.intervalMs = 1000;
        return config;
    }
}
