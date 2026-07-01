package com.younjaeh.kindmap.gamma;

import com.younjaeh.kindmap.mixin.OptionInstanceAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;

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
    @SuppressWarnings("unchecked")
    public void setBrightness(double value) {
        OptionInstance<Double> gamma = client.options.gamma();
        if (value <= 1.0) {
            gamma.set(value);
            return;
        }

        ((OptionInstanceAccessor<Double>) (Object) gamma).kindmap$setValue(value);
    }
}
