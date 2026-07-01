package com.younjaeh.kindmap.macro;

import com.younjaeh.kindmap.config.MacroConfig;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MacroKeySetTest {
    @Test
    void keepsUniqueNonBlankConfiguredKeysInOrder() {
        MacroConfig first = macro("key.keyboard.g");
        MacroConfig blank = macro(" ");
        MacroConfig duplicate = macro("key.keyboard.g");
        MacroConfig second = macro("key.keyboard.grave.accent");

        assertEquals(
                List.of("key.keyboard.g", "key.keyboard.grave.accent"),
                MacroKeySet.from(Arrays.asList(first, null, blank, duplicate, second))
        );
    }

    private static MacroConfig macro(String key) {
        MacroConfig macro = MacroConfig.defaults();
        macro.key = key;
        return macro;
    }
}
