package com.younjaeh.kindmap.ui;

import com.younjaeh.kindmap.config.MacroConfig;
import com.younjaeh.kindmap.config.ModConfig;
import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

final class KindMapConfigDraftTest {
    @Test
    void copiesAndAppliesConfigWithoutSharingMacroObjects() {
        ModConfig live = ModConfig.defaults();
        live.gamma.enabled = true;
        live.gamma.enabledValue = 100.0;
        MacroConfig liveMacro = MacroConfig.defaults();
        liveMacro.id = "first";
        liveMacro.name = "First";
        liveMacro.key = "key.keyboard.g";
        liveMacro.content = "/spawn";
        live.macros.add(liveMacro);

        ModConfig draft = KindMapConfigDraft.copyOf(live);
        draft.gamma.enabled = false;
        draft.gamma.enabledValue = 1250.0;
        draft.macros.getFirst().name = "Edited";
        draft.macros.getFirst().key = "key.keyboard.grave.accent";
        draft.macros.getFirst().action = MacroAction.TYPE;
        draft.macros.getFirst().mode = MacroMode.TOGGLE;
        draft.macros.getFirst().delayMs = 250L;
        draft.macros.getFirst().intervalMs = 500L;

        assertEquals("First", live.macros.getFirst().name);

        KindMapConfigDraft.applyTo(live, draft);

        assertFalse(live.gamma.enabled);
        assertEquals(1250.0, live.gamma.enabledValue);
        assertEquals("Edited", live.macros.getFirst().name);
        assertEquals("key.keyboard.grave.accent", live.macros.getFirst().key);
        assertEquals(MacroAction.TYPE, live.macros.getFirst().action);
        assertEquals(MacroMode.TOGGLE, live.macros.getFirst().mode);
        assertEquals(250L, live.macros.getFirst().delayMs);
        assertEquals(500L, live.macros.getFirst().intervalMs);
        assertNotSame(draft.macros.getFirst(), live.macros.getFirst());
    }

    @Test
    void parsesEditableNumbersWithFallbacksAndClamping() {
        assertEquals(1500.0, KindMapConfigDraft.parseDouble("oops", 1500.0, 0.0, 1500.0));
        assertEquals(0.0, KindMapConfigDraft.parseDouble("-5", 100.0, 0.0, 1500.0));
        assertEquals(1500.0, KindMapConfigDraft.parseDouble("9999", 100.0, 0.0, 1500.0));
        assertEquals(42.5, KindMapConfigDraft.parseDouble("42.5", 100.0, 0.0, 1500.0));

        assertEquals(300L, KindMapConfigDraft.parseLong("bad", 300L, 0L));
        assertEquals(0L, KindMapConfigDraft.parseLong("-1", 300L, 0L));
        assertEquals(750L, KindMapConfigDraft.parseLong("750", 300L, 0L));
    }
}
