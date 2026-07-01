package com.younjaeh.kindmap.macro;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;

public final class MinecraftChatExecutor implements ChatExecutor {
    private final Minecraft client;

    public MinecraftChatExecutor(Minecraft client) {
        this.client = client;
    }

    public static MinecraftChatExecutor currentClient() {
        return new MinecraftChatExecutor(Minecraft.getInstance());
    }

    @Override
    public void send(String content) {
        if (content == null || client == null || client.player == null) {
            return;
        }

        ClientPacketListener connection = client.getConnection();
        if (connection == null) {
            return;
        }

        if (content.startsWith("/")) {
            connection.sendCommand(content.substring(1));
        } else {
            connection.sendChat(content);
        }
    }

    @Override
    public void type(String content) {
        if (content == null || client == null || client.player == null) {
            return;
        }

        client.setScreen(new ChatScreen(content, false));
    }
}
