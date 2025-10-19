package me.duncanruns.fsgmod.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import me.voidxwalker.autoreset.Atum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Atum.class, remap = false)
public abstract class AtumMixin {
    @ModifyReturnValue(method = "inDemoMode", at = @At("RETURN"))
    private static boolean disableDemoMode(boolean inDemoMode) {
        return false;
    }

    @Redirect(method = "setSeedProvider", at = @At(value = "INVOKE", target = "Lme/voidxwalker/autoreset/Atum;ensureState(ZLjava/lang/String;)V"))
    private static void ensureState(boolean condition, String exceptionMessage) throws IllegalStateException {
        if (Atum.isRunning()) {
            throw new IllegalStateException(exceptionMessage);
        }
    }
}
