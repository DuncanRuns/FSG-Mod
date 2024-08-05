package me.duncanruns.fsgmod.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import me.duncanruns.fsgmod.FSGFilterResult;
import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.SeedManager;
import me.duncanruns.fsgmod.screen.FilterFailedScreen;
import me.duncanruns.fsgmod.screen.FilteringScreen;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreateWorldScreen.class, priority = 1500)
public abstract class CreateWorldScreenMixin extends Screen {
    protected CreateWorldScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void interruptCreationMixin(CallbackInfo info) {
        if (!Atum.isRunning) return;

        if (SeedManager.hasFailed()) {
            minecraft.openScreen(new FilterFailedScreen());
            info.cancel();
            return;
        }

        if (SeedManager.canTake()) {
            FSGFilterResult filterResult = SeedManager.take();
            FSGMod.setLastToken(filterResult.token);
            Atum.seed = filterResult.seed;
        } else {
            SeedManager.find();
            minecraft.openScreen(new FilteringScreen());
            info.cancel();
        }
    }


    @TargetHandler(
            mixin = "me.voidxwalker.autoreset.mixin.CreateWorldScreenMixin",
            name = "injected"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lme/voidxwalker/autoreset/Atum;log(Lorg/apache/logging/log4j/Level;Ljava/lang/String;)V"))
    private void replaceAtumLog(Level level, String message) {
        Atum.log(level, message.startsWith("Resetting the set seed") ? "Resetting a filtered seed" : message);
    }
}
