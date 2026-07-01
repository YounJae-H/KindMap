package com.younjaeh.kindmap.mixin;

import com.younjaeh.kindmap.KindMapClient;
import com.younjaeh.kindmap.gamma.GammaSliderRange;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.Options;
import net.minecraft.client.gui.components.AbstractOptionSliderButton;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Consumer;

@Mixin(OptionInstance.OptionInstanceSliderButton.class)
public abstract class GammaSliderButtonMixin extends AbstractOptionSliderButton {
    @Shadow
    @Final
    private OptionInstance<Double> instance;

    @Shadow
    @Final
    private Consumer<Double> onValueChanged;

    @Shadow
    private Long delayedApplyAt;

    protected GammaSliderButtonMixin(Options options, int x, int y, int width, int height, double value) {
        super(options, x, y, width, height, value);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void kindmap$normalizeGammaSlider(CallbackInfo callbackInfo) {
        if (!kindmap$isManagedGammaSlider()) {
            return;
        }

        kindmap$syncSliderFromOption();
    }

    @Inject(method = "updateMessage", at = @At("HEAD"), cancellable = true)
    private void kindmap$updateGammaMessage(CallbackInfo callbackInfo) {
        if (!kindmap$isManagedGammaSlider()) {
            return;
        }

        int percent = GammaSliderRange.displayPercentFromRawBrightness(kindmap$currentRawBrightness());
        setMessage(Options.genericValueLabel(Component.translatable("options.gamma"), Component.literal(percent + "%")));
        setTooltip(null);
        callbackInfo.cancel();
    }

    @Inject(method = "applyUnsavedValue", at = @At("HEAD"), cancellable = true)
    @SuppressWarnings("unchecked")
    private void kindmap$applyGammaSliderValue(CallbackInfo callbackInfo) {
        if (!kindmap$isManagedGammaSlider()) {
            return;
        }

        double rawBrightness = GammaSliderRange.rawBrightnessFromSliderValue(this.value, kindmap$maxPercent());
        if (rawBrightness <= 1.0) {
            instance.set(rawBrightness);
        } else {
            ((OptionInstanceAccessor<Double>) (Object) instance).kindmap$setValue(rawBrightness);
        }
        onValueChanged.accept(instance.get());

        KindMapClient client = KindMapClient.instance();
        if (client != null) {
            client.syncEnabledGammaFromVanillaSlider(rawBrightness);
        }

        callbackInfo.cancel();
    }

    @Inject(method = "resetValue", at = @At("HEAD"), cancellable = true)
    private void kindmap$resetGammaSliderValue(CallbackInfo callbackInfo) {
        if (!kindmap$isManagedGammaSlider()) {
            return;
        }

        kindmap$syncSliderFromOption();
        callbackInfo.cancel();
    }

    @Unique
    private boolean kindmap$isManagedGammaSlider() {
        KindMapClient client = KindMapClient.instance();
        return client != null && client.isGammaEnabled() && instance == this.options.gamma();
    }

    @Unique
    private void kindmap$syncSliderFromOption() {
        this.value = GammaSliderRange.sliderValueFromRawBrightness(instance.get(), kindmap$maxPercent());
        this.delayedApplyAt = null;
        updateMessage();
    }

    @Unique
    private double kindmap$currentRawBrightness() {
        if (this.value > 1.0) {
            return instance.get();
        }
        return GammaSliderRange.rawBrightnessFromSliderValue(this.value, kindmap$maxPercent());
    }

    @Unique
    private double kindmap$maxPercent() {
        KindMapClient client = KindMapClient.instance();
        return client == null ? 1500.0 : client.gammaMaxPercent();
    }
}
