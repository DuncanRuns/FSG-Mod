package me.duncanruns.fsgwrappermod.mixin;

import me.duncanruns.fsgwrappermod.FSGFilterResult;
import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import me.duncanruns.fsgwrappermod.SeedManager;
import me.duncanruns.fsgwrappermod.screen.FilterFailedScreen;
import me.duncanruns.fsgwrappermod.screen.FilteringScreen;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin extends Screen {

    protected CreateWorldScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void interruptCreationMixin(CallbackInfo info) {
        if (!Atum.isRunning) return;

        if (SeedManager.hasFailed()) {
            client.openScreen(new FilterFailedScreen());
            info.cancel();
            return;
        }

        if (SeedManager.canTake()) {
            FSGFilterResult filterResult = SeedManager.take();
            FSGWrapperMod.setLastToken(filterResult.token);
            SeedManager.find();
            Atum.seed = filterResult.seed;
        } else {
            SeedManager.find();
            client.openScreen(new FilteringScreen());
            info.cancel();
        }
    }
}
