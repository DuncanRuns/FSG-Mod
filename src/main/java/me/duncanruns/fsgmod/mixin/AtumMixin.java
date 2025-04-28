package me.duncanruns.fsgmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.voidxwalker.autoreset.Atum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Atum.class, remap = false)
public abstract class AtumMixin {
    @ModifyReturnValue(method = "inDemoMode", at = @At("RETURN"))
    private static boolean disableDemoMode(boolean inDemoMode) {
        return false;
    }
}
