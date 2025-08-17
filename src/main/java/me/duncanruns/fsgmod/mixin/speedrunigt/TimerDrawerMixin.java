package me.duncanruns.fsgmod.mixin.speedrunigt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.TimerDrawer;
import com.redlimerl.speedrunigt.timer.running.RunType;
import me.duncanruns.fsgmod.FSGMod;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true, print = true)
@Mixin(value = TimerDrawer.class, remap = false)
public class TimerDrawerMixin {
    @WrapOperation(method = "getIGTText", at = @At(value = "INVOKE", target = "Lcom/redlimerl/speedrunigt/timer/InGameTimer;getRunType()Lcom/redlimerl/speedrunigt/timer/running/RunType;"))
    private RunType showRetimeOnSetSeed(InGameTimer instance, Operation<RunType> original) {
        RunType originalVal = original.call(instance);
        if (FSGMod.shouldRetime && originalVal == RunType.SET_SEED) return RunType.RANDOM_SEED;
        return originalVal;
    }
}
