package me.duncanruns.fsgmod.mixin.speedrunigt;

import com.llamalad7.mixinextras.sugar.Local;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.running.RunType;
import me.duncanruns.fsgmod.FSGMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = InGameTimer.class, remap = false)
public abstract class InGameTimerMixin {

    @Shadow
    private long retimedIGTTime;

    @Shadow
    public abstract RunType getRunType();

    @Inject(method = "getRetimedInGameTime(Z)J", at = @At(value = "INVOKE", target = "Lcom/redlimerl/speedrunigt/timer/category/RunCategory;isNeedAutoRetime(Lcom/redlimerl/speedrunigt/timer/InGameTimer;)Z"), cancellable = true)
    private void forceRetime(boolean override, CallbackInfoReturnable<Long> cir, @Local long base) {
        if (FSGMod.shouldRetime && getRunType() == RunType.SET_SEED) {
            cir.setReturnValue(base + retimedIGTTime);
        }
    }
}
