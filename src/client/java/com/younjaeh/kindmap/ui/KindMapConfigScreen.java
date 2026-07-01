package com.younjaeh.kindmap.ui;

import com.mojang.blaze3d.platform.InputConstants;
import com.younjaeh.kindmap.KindMapClient;
import com.younjaeh.kindmap.config.MacroConfig;
import com.younjaeh.kindmap.config.ModConfig;
import com.younjaeh.kindmap.macro.MacroAction;
import com.younjaeh.kindmap.macro.MacroMode;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

public final class KindMapConfigScreen extends Screen {
    private static final int FORM_WIDTH = 520;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;
    private final KindMapClient client;
    private final ModConfig draft;

    private int selectedMacroIndex;
    private boolean capturingKey;
    private EditBox gammaValueField;
    private EditBox macroNameField;
    private EditBox macroKeyField;
    private EditBox macroContentField;
    private EditBox macroDelayField;
    private EditBox macroIntervalField;
    private Button captureKeyButton;

    private KindMapConfigScreen(Screen parent, KindMapClient client) {
        super(Component.translatable("text.kindmap.title"));
        this.parent = parent;
        this.client = client;
        this.draft = KindMapConfigDraft.copyOf(client == null ? ModConfig.defaults() : client.config());
    }

    public static Screen create(Screen parent) {
        return new KindMapConfigScreen(parent, KindMapClient.instance());
    }

    @Override
    protected void init() {
        clampSelectedMacroIndex();

        int formWidth = Math.min(FORM_WIDTH, this.width - 24);
        int left = (this.width - formWidth) / 2;
        int y = 10;

        addRenderableWidget(new StringWidget(left, y, formWidth, WIDGET_HEIGHT, Component.translatable("text.kindmap.title"), this.font));
        y += 28;

        addLabel(left, y, 160, Component.translatable("text.kindmap.gamma"));
        y += 22;

        addRenderableWidget(CycleButton.onOffBuilder(draft.gamma.enabled)
                .create(left, y, 160, WIDGET_HEIGHT, Component.translatable("text.kindmap.gamma.enabled"), (button, value) -> draft.gamma.enabled = value));
        addLabel(left + 176, y, 120, Component.translatable("text.kindmap.gamma.enabled_value"));
        gammaValueField = addEditBox(left + 304, y, 100, 32, Double.toString(draft.gamma.enabledValue),
                Component.translatable("text.kindmap.gamma.enabled_value"),
                value -> draft.gamma.enabledValue = parseGammaValue(value));
        y += 34;

        addLabel(left, y, 160, Component.translatable("text.kindmap.macros"));
        y += 22;

        Button selector = Button.builder(macroSelectorLabel(), button -> selectNextMacro())
                .bounds(left, y, 240, WIDGET_HEIGHT)
                .build();
        selector.active = !draft.macros.isEmpty();
        addRenderableWidget(selector);
        addRenderableWidget(Button.builder(Component.translatable("text.kindmap.add_macro"), button -> addMacro())
                .bounds(left + 252, y, 120, WIDGET_HEIGHT)
                .build());
        Button deleteButton = Button.builder(Component.translatable("text.kindmap.delete_macro"), button -> deleteSelectedMacro())
                .bounds(left + 384, y, 120, WIDGET_HEIGHT)
                .build();
        deleteButton.active = selectedMacro() != null;
        addRenderableWidget(deleteButton);
        y += 34;

        MacroConfig macro = selectedMacro();
        if (macro == null) {
            addRenderableWidget(new StringWidget(left, y, formWidth, WIDGET_HEIGHT, Component.translatable("text.kindmap.no_macros"), this.font));
        } else {
            addMacroFields(left, y, formWidth, macro);
        }

        int buttonY = this.height - 28;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
                .bounds(left + formWidth - 204, buttonY, 96, WIDGET_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(Component.translatable("gui.cancel"), button -> closeWithoutSaving())
                .bounds(left + formWidth - 100, buttonY, 100, WIDGET_HEIGHT)
                .build());
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (capturingKey) {
            InputConstants.Key key = InputConstants.getKey(event);
            MacroConfig macro = selectedMacro();
            if (macro != null && key != InputConstants.UNKNOWN) {
                macro.key = key.getName();
                if (macroKeyField != null) {
                    macroKeyField.setValue(macro.key);
                }
            }
            capturingKey = false;
            refreshWidgets();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public void onClose() {
        closeWithoutSaving();
    }

    private void addMacroFields(int left, int y, int formWidth, MacroConfig macro) {
        addRenderableWidget(CycleButton.onOffBuilder(macro.enabled)
                .create(left, y, 160, WIDGET_HEIGHT, Component.translatable("text.kindmap.macro.enabled"), (button, value) -> macro.enabled = value));
        addLabel(left + 176, y, 60, Component.translatable("text.kindmap.macro.name"));
        macroNameField = addEditBox(left + 240, y, formWidth - 240, 80, valueOrEmpty(macro.name),
                Component.translatable("text.kindmap.macro.name"), value -> macro.name = value);
        y += 30;

        addLabel(left, y, 60, Component.translatable("text.kindmap.macro.key"));
        macroKeyField = addEditBox(left + 64, y, 296, 128, valueOrEmpty(macro.key),
                Component.translatable("text.kindmap.macro.key"), value -> macro.key = value);
        captureKeyButton = Button.builder(captureKeyLabel(), button -> beginKeyCapture())
                .bounds(left + 372, y, 132, WIDGET_HEIGHT)
                .build();
        addRenderableWidget(captureKeyButton);
        y += 30;

        addLabel(left, y, 70, Component.translatable("text.kindmap.macro.content"));
        macroContentField = addEditBox(left + 76, y, formWidth - 76, 256, valueOrEmpty(macro.content),
                Component.translatable("text.kindmap.macro.content"), value -> macro.content = value);
        y += 30;

        addRenderableWidget(CycleButton.builder(value -> Component.literal(value.name()), macro.action == null ? MacroAction.SEND : macro.action)
                .withValues(MacroAction.values())
                .create(left, y, 240, WIDGET_HEIGHT, Component.translatable("text.kindmap.macro.action"), (button, value) -> macro.action = value));
        addRenderableWidget(CycleButton.builder(value -> Component.literal(value.name()), macro.mode == null ? MacroMode.SIMPLE : macro.mode)
                .withValues(MacroMode.values())
                .create(left + 264, y, 240, WIDGET_HEIGHT, Component.translatable("text.kindmap.macro.mode"), (button, value) -> macro.mode = value));
        y += 30;

        addLabel(left, y, 70, Component.translatable("text.kindmap.macro.delay_ms"));
        macroDelayField = addEditBox(left + 76, y, 120, 20, Long.toString(macro.delayMs),
                Component.translatable("text.kindmap.macro.delay_ms"), value -> macro.delayMs = parseMacroLong(value, macro.delayMs));
        addLabel(left + 216, y, 80, Component.translatable("text.kindmap.macro.interval_ms"));
        macroIntervalField = addEditBox(left + 304, y, 120, 20, Long.toString(macro.intervalMs),
                Component.translatable("text.kindmap.macro.interval_ms"), value -> macro.intervalMs = parseMacroLong(value, macro.intervalMs));
    }

    private void addLabel(int x, int y, int width, Component label) {
        addRenderableWidget(new StringWidget(x, y, width, WIDGET_HEIGHT, label, this.font));
    }

    private EditBox addEditBox(int x, int y, int width, int maxLength, String value, Component hint, java.util.function.Consumer<String> responder) {
        EditBox editBox = new EditBox(this.font, x, y, width, WIDGET_HEIGHT, hint);
        editBox.setMaxLength(maxLength);
        editBox.setValue(value);
        editBox.setHint(hint);
        editBox.setResponder(responder);
        return addRenderableWidget(editBox);
    }

    private void addMacro() {
        commitCurrentFieldValues();
        draft.macros.add(KindMapConfigDraft.newMacro());
        selectedMacroIndex = draft.macros.size() - 1;
        capturingKey = false;
        refreshWidgetsWithoutCommit();
    }

    private void deleteSelectedMacro() {
        MacroConfig macro = selectedMacro();
        if (macro == null) {
            return;
        }
        draft.macros.remove(selectedMacroIndex);
        selectedMacroIndex = Math.max(0, selectedMacroIndex - 1);
        capturingKey = false;
        refreshWidgetsWithoutCommit();
    }

    private void selectNextMacro() {
        if (draft.macros.isEmpty()) {
            return;
        }
        commitCurrentFieldValues();
        selectedMacroIndex = (selectedMacroIndex + 1) % draft.macros.size();
        capturingKey = false;
        refreshWidgetsWithoutCommit();
    }

    private void beginKeyCapture() {
        commitCurrentFieldValues();
        capturingKey = true;
        if (captureKeyButton != null) {
            captureKeyButton.setMessage(captureKeyLabel());
        }
    }

    private void saveAndClose() {
        commitCurrentFieldValues();
        if (client != null && client.config() != null) {
            KindMapConfigDraft.applyTo(client.config(), draft);
            client.saveConfig();
            client.applyGammaFromConfig();
            client.reloadMacrosFromConfig();
        }
        closeWithoutSaving();
    }

    private void closeWithoutSaving() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(parent);
        }
    }

    private void refreshWidgets() {
        commitCurrentFieldValues();
        refreshWidgetsWithoutCommit();
    }

    private void refreshWidgetsWithoutCommit() {
        clearWidgets();
        init();
    }

    private void commitCurrentFieldValues() {
        if (gammaValueField != null) {
            draft.gamma.enabledValue = parseGammaValue(gammaValueField.getValue());
        }

        MacroConfig macro = selectedMacro();
        if (macro == null) {
            return;
        }
        if (macroNameField != null) {
            macro.name = macroNameField.getValue();
        }
        if (macroKeyField != null) {
            macro.key = macroKeyField.getValue();
        }
        if (macroContentField != null) {
            macro.content = macroContentField.getValue();
        }
        if (macroDelayField != null) {
            macro.delayMs = parseMacroLong(macroDelayField.getValue(), macro.delayMs);
        }
        if (macroIntervalField != null) {
            macro.intervalMs = parseMacroLong(macroIntervalField.getValue(), macro.intervalMs);
        }
    }

    private MacroConfig selectedMacro() {
        if (draft.macros.isEmpty() || selectedMacroIndex < 0 || selectedMacroIndex >= draft.macros.size()) {
            return null;
        }
        return draft.macros.get(selectedMacroIndex);
    }

    private void clampSelectedMacroIndex() {
        if (draft.macros.isEmpty()) {
            selectedMacroIndex = 0;
            return;
        }
        if (selectedMacroIndex < 0) {
            selectedMacroIndex = 0;
        }
        if (selectedMacroIndex >= draft.macros.size()) {
            selectedMacroIndex = draft.macros.size() - 1;
        }
    }

    private Component macroSelectorLabel() {
        MacroConfig macro = selectedMacro();
        if (macro == null) {
            return Component.translatable("text.kindmap.no_macros");
        }
        return Component.translatable("text.kindmap.macro_selector", selectedMacroIndex + 1, draft.macros.size(), valueOrEmpty(macro.name));
    }

    private Component captureKeyLabel() {
        return Component.translatable(capturingKey ? "text.kindmap.macro.capture_waiting" : "text.kindmap.macro.capture_key");
    }

    private double parseGammaValue(String value) {
        return KindMapConfigDraft.parseDouble(value, draft.gamma.enabledValue, draft.gamma.minValue, draft.gamma.maxValue);
    }

    private long parseMacroLong(String value, long fallback) {
        return KindMapConfigDraft.parseLong(value, fallback, 0L);
    }

    private static String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }
}
