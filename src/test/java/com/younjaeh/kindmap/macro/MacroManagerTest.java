package com.younjaeh.kindmap.macro;

import com.younjaeh.kindmap.config.MacroConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class MacroManagerTest {
    private static final String GRAVE = "key.keyboard.grave.accent";
    private static final String ENDER = "/엔더";

    private final RecordingChatExecutor chatExecutor = new RecordingChatExecutor();
    private final MacroManager manager = new MacroManager(chatExecutor);

    @Test
    void simpleMacroSendsContent() {
        manager.setMacros(List.of(macro("simple", MacroAction.SEND, MacroMode.SIMPLE)));

        manager.onKeyPressed(GRAVE, 1000L, false);

        assertEquals(List.of("SEND:" + ENDER), chatExecutor.events);
    }

    @Test
    void typeMacroTypesContent() {
        manager.setMacros(List.of(macro("type", MacroAction.TYPE, MacroMode.SIMPLE)));

        manager.onKeyPressed(GRAVE, 1000L, false);

        assertEquals(List.of("TYPE:" + ENDER), chatExecutor.events);
    }

    @Test
    void delayedMacroRunsAfterDelay() {
        MacroConfig macro = macro("delayed", MacroAction.SEND, MacroMode.DELAYED);
        macro.delayMs = 250L;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.tick(1249L);
        assertEquals(List.of(), chatExecutor.events);

        manager.tick(1250L);
        assertEquals(List.of("SEND:" + ENDER), chatExecutor.events);

        manager.tick(1500L);
        assertEquals(List.of("SEND:" + ENDER), chatExecutor.events);
    }

    @Test
    void doesNotRunWhileTypingFocused() {
        manager.setMacros(List.of(macro("typing", MacroAction.SEND, MacroMode.SIMPLE)));

        manager.onKeyPressed(GRAVE, 1000L, true);

        assertEquals(List.of(), chatExecutor.events);
    }

    @Test
    void disabledOrBlankMacrosDoNotRun() {
        MacroConfig disabled = macro("disabled", MacroAction.SEND, MacroMode.SIMPLE);
        disabled.enabled = false;
        MacroConfig blankKey = macro("blank-key", MacroAction.SEND, MacroMode.SIMPLE);
        blankKey.key = " ";
        MacroConfig blankContent = macro("blank-content", MacroAction.SEND, MacroMode.SIMPLE);
        blankContent.content = " ";
        manager.setMacros(List.of(disabled, blankKey, blankContent));

        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.onKeyPressed(" ", 1000L, false);

        assertEquals(List.of(), chatExecutor.events);
    }

    @Test
    void repeatingMacroRunsWhileHeldUsingInterval() {
        MacroConfig macro = macro("repeat", MacroAction.SEND, MacroMode.REPEATING);
        macro.intervalMs = 200L;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.onKeyHeld(GRAVE, 1000L, false);
        manager.onKeyHeld(GRAVE, 1199L, false);
        assertEquals(List.of("SEND:" + ENDER), chatExecutor.events);

        manager.onKeyHeld(GRAVE, 1200L, false);
        manager.onKeyHeld(GRAVE, 1399L, false);
        assertEquals(List.of("SEND:" + ENDER, "SEND:" + ENDER), chatExecutor.events);

        manager.onKeyHeld(GRAVE, 1400L, true);
        assertEquals(List.of("SEND:" + ENDER, "SEND:" + ENDER), chatExecutor.events);
    }

    @Test
    void toggleMacroRepeatsUntilPressedAgain() {
        MacroConfig macro = macro("toggle", MacroAction.SEND, MacroMode.TOGGLE);
        macro.intervalMs = 200L;
        manager.setMacros(List.of(macro));

        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.tick(1199L);
        assertEquals(List.of(), chatExecutor.events);

        manager.tick(1200L);
        assertEquals(List.of("SEND:" + ENDER), chatExecutor.events);

        manager.tick(1400L);
        assertEquals(List.of("SEND:" + ENDER, "SEND:" + ENDER), chatExecutor.events);

        manager.onKeyPressed(GRAVE, 1500L, false);
        manager.tick(1600L);
        assertEquals(List.of("SEND:" + ENDER, "SEND:" + ENDER), chatExecutor.events);
    }

    @Test
    void setMacrosClearsRuntimeState() {
        MacroConfig delayed = macro("delayed", MacroAction.SEND, MacroMode.DELAYED);
        delayed.delayMs = 250L;
        MacroConfig toggle = macro("toggle", MacroAction.SEND, MacroMode.TOGGLE);
        toggle.intervalMs = 200L;
        manager.setMacros(List.of(delayed, toggle));

        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.onKeyPressed(GRAVE, 1000L, false);
        manager.setMacros(List.of(macro("replacement", MacroAction.SEND, MacroMode.SIMPLE)));
        manager.tick(1250L);
        manager.tick(1400L);

        assertEquals(List.of(), chatExecutor.events);
    }

    private static MacroConfig macro(String id, MacroAction action, MacroMode mode) {
        MacroConfig macro = MacroConfig.defaults();
        macro.id = id;
        macro.name = id;
        macro.key = GRAVE;
        macro.content = ENDER;
        macro.action = action;
        macro.mode = mode;
        return macro;
    }

    private static final class RecordingChatExecutor implements ChatExecutor {
        private final List<String> events = new ArrayList<>();

        @Override
        public void send(String content) {
            events.add("SEND:" + content);
        }

        @Override
        public void type(String content) {
            events.add("TYPE:" + content);
        }
    }
}
