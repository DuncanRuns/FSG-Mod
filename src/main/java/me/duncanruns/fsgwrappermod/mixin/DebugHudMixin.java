package me.duncanruns.fsgwrappermod.mixin;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Inject(method = "getRightText", at = @At("RETURN"))
    private void avth(CallbackInfoReturnable<List<String>> info) {
        info.getReturnValue().add(String.format("FSGWM v%s %d", FSGWrapperMod.VERSION, FSGWrapperMod.lastTokenHash));
    }
}
