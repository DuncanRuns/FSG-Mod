package me.duncanruns.fsgmod.mixin;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.SeedManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.hud.BackgroundHelper;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Shadow
    @Final
    public TextRenderer textRenderer;

    @Shadow
    @Final
    private Window window;

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/toast/ToastManager;draw(Lnet/minecraft/client/util/math/MatrixStack;)V", shift = At.Shift.AFTER))
    private void drawFilterCount(boolean tick, CallbackInfo ci) {
        if (!FSGMod.DEBUG) return;
        String text = "Filtering: " + SeedManager.getCurrentlyFiltering();
        this.textRenderer.draw(new MatrixStack(), text, ((this.window.getScaledWidth() - this.textRenderer.getWidth(text)) / 2f), this.window.getScaledHeight() - 12, BackgroundHelper.ColorMixer.getArgb(255, 255, 255, 255));
    }
}
