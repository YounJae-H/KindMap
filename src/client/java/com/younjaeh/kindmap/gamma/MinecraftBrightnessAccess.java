package com.younjaeh.kindmap.gamma;

import net.minecraft.client.Minecraft;

import java.util.Objects;

public final class MinecraftBrightnessAccess implements BrightnessAccess {
    private final Minecraft client;

    public MinecraftBrightnessAccess(Minecraft client) {
        this.client = Objects.requireNonNull(client, "client");
    }

    public static MinecraftBrightnessAccess currentClient() {
        return new MinecraftBrightnessAccess(Minecraft.getInstance());
    }

    @Override
    public double getBrightness() {
        return client.options.gamma().get();
    }

    @Override
    public void setBrightness(double value) {
        client.options.gamma().set(value);
    }
}
