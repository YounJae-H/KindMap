package com.younjaeh.kindmap.gamma;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.Objects;

public final class MinecraftGammaNotifier implements GammaNotifier {
    private final Minecraft client;

    public MinecraftGammaNotifier(Minecraft client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    public static MinecraftGammaNotifier currentClient() {
        return new MinecraftGammaNotifier(Minecraft.getInstance());
    }

    @Override
    public void showGammaPercent(int percent) {
        if (client.gui == null) {
            return;
        }

        client.gui.setOverlayMessage(Component.literal("\uAC10\uB9C8: " + percent + "%").withStyle(ChatFormatting.GOLD), false);
    }
}
