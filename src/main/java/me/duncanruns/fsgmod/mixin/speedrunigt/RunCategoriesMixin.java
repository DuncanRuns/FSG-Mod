package me.duncanruns.fsgmod.mixin.speedrunigt;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.redlimerl.speedrunigt.timer.InGameTimer;
import com.redlimerl.speedrunigt.timer.category.RunCategories;
import com.redlimerl.speedrunigt.timer.running.RunType;
import me.duncanruns.fsgmod.FSGMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(RunCategories.class)
public abstract class RunCategoriesMixin {
    @WrapOperation(method = "lambda$static$0", at = @At(value = "INVOKE", target = "Lcom/redlimerl/speedrunigt/timer/InGameTimer;getRunType()Lcom/redlimerl/speedrunigt/timer/running/RunType;"), remap = false)
    private static RunType showRetimeOnSetSeed(InGameTimer instance, Operation<RunType> original) {
        RunType originalVal = original.call(instance);
        if (FSGMod.shouldRetime && originalVal == RunType.SET_SEED) return RunType.RANDOM_SEED;
        return originalVal;
    }
}
