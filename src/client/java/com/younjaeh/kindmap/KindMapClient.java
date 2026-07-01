package com.younjaeh.kindmap;

import com.mojang.blaze3d.platform.InputConstants;
import com.younjaeh.kindmap.config.ConfigManager;
import com.younjaeh.kindmap.config.ModConfig;
import com.younjaeh.kindmap.gamma.GammaController;
import com.younjaeh.kindmap.gamma.MinecraftBrightnessAccess;
import com.younjaeh.kindmap.macro.MacroKeySet;
import com.younjaeh.kindmap.macro.MacroManager;
import com.younjaeh.kindmap.macro.MinecraftChatExecutor;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public final class KindMapClient implements ClientModInitializer {
    private static final String GAMMA_KEY_TRANSLATION = "key.kindmap.toggle_gamma";
    private static final KeyMapping.Category KEY_CATEGORY =
            KeyMapping.Category.register(Identifier.withDefaultNamespace(ModConstants.MOD_ID));

    private static KindMapClient instance;

    private ConfigManager configManager;
    private ModConfig config;
    private GammaController gammaController;
    private MacroManager macroManager;
    private KeyMapping gammaKeyMapping;
    private final Map<String, Boolean> macroPressedKeys = new HashMap<>();

    @Override
    public void onInitializeClient() {
        instance = this;

        Path configPath = FabricLoader.getInstance().getConfigDir().resolve(ModConstants.CONFIG_FILE_NAME);
        configManager = new ConfigManager(configPath);
        config = loadConfig();
        gammaController = new GammaController(config.gamma, MinecraftBrightnessAccess.currentClient(), this::saveConfig);
        macroManager = new MacroManager(MinecraftChatExecutor.currentClient());
        reloadMacrosFromConfig();

        gammaKeyMapping = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                GAMMA_KEY_TRANSLATION,
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                KEY_CATEGORY
        ));

        gammaController.initialize();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndClientTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> saveConfig());
    }

    public static KindMapClient instance() {
        return instance;
    }

    public ModConfig config() {
        return config;
    }

    public void saveConfig() {
        if (configManager == null || config == null) {
            return;
        }

        try {
            configManager.save(config);
        } catch (IOException ignored) {
        }
    }

    public void reloadMacrosFromConfig() {
        if (macroManager == null || config == null) {
            return;
        }

        macroManager.setMacros(config.macros);
        macroPressedKeys.clear();
    }

    private ModConfig loadConfig() {
        try {
            return configManager.load();
        } catch (IOException exception) {
            return ModConfig.defaults();
        }
    }

    private void onEndClientTick(Minecraft client) {
        while (gammaKeyMapping.consumeClick()) {
            gammaController.toggle();
        }

        long nowMs = System.currentTimeMillis();
        boolean typingFocused = isTypingFocused(client);
        macroManager.tick(nowMs, typingFocused);
        pollMacroKeys(client, nowMs, typingFocused);
    }

    private void pollMacroKeys(Minecraft client, long nowMs, boolean typingFocused) {
        List<String> keys = MacroKeySet.from(config.macros);
        macroPressedKeys.keySet().retainAll(new HashSet<>(keys));
        for (String key : keys) {
            Integer keyCode = keyCode(key);
            if (keyCode == null) {
                macroPressedKeys.remove(key);
                continue;
            }

            boolean pressed = InputConstants.isKeyDown(client.getWindow(), keyCode);
            boolean wasPressed = macroPressedKeys.getOrDefault(key, false);
            if (pressed && !wasPressed) {
                macroManager.onKeyPressed(key, nowMs, typingFocused);
            }
            if (pressed) {
                macroManager.onKeyHeld(key, nowMs, typingFocused);
            }
            macroPressedKeys.put(key, pressed);
        }
    }

    private static boolean isTypingFocused(Minecraft client) {
        if (client.screen == null) {
            return false;
        }

        if (client.screen instanceof ChatScreen) {
            return true;
        }
        return true;
    }

    private static Integer keyCode(String translationKey) {
        if (translationKey == null || translationKey.isBlank()) {
            return null;
        }

        try {
            InputConstants.Key key = InputConstants.getKey(translationKey);
            if (key == InputConstants.UNKNOWN) {
                return null;
            }
            return key.getValue();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
