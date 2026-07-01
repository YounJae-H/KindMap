package com.younjaeh.kindmap.config;

import java.util.ArrayList;
import java.util.List;

public final class ModConfig {
    public GammaConfig gamma;
    public List<MacroConfig> macros;

    public static ModConfig defaults() {
        ModConfig config = new ModConfig();
        config.gamma = GammaConfig.defaults();
        config.macros = new ArrayList<>();
        return config;
    }
}
