package me.duncanruns.fsgmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Atum.class, remap = false)
public abstract class AtumMixin {

    @Inject(method = "stopRunning", at = @At("TAIL"))
    private static void onStop(CallbackInfo ci) {
        SeedManager.cancelAll();
    }

    @ModifyReturnValue(method = "inDemoMode", at = @At("RETURN"))
    private static boolean disableDemoMode(boolean inDemoMode) {
        return false;
    }
}
