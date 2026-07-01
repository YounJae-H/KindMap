package com.younjaeh.kindmap.macro;

import com.younjaeh.kindmap.config.MacroConfig;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class MacroKeySet {
    private MacroKeySet() {
    }

    public static List<String> from(Iterable<MacroConfig> macros) {
        ArrayList<String> keys = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        if (macros == null) {
            return keys;
        }

        for (MacroConfig macro : macros) {
            if (macro == null || macro.key == null || macro.key.isBlank()) {
                continue;
            }
            if (seen.add(macro.key)) {
                keys.add(macro.key);
            }
        }
        return keys;
    }
}
