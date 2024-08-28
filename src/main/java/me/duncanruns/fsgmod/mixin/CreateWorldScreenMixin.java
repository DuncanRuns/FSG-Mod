package me.duncanruns.fsgmod.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import me.duncanruns.fsgmod.FSGFilterResult;
import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.SeedManager;
import me.duncanruns.fsgmod.screen.FilterFailedScreen;
import me.duncanruns.fsgmod.screen.FilteringScreen;
import me.voidxwalker.autoreset.Atum;
import me.voidxwalker.autoreset.interfaces.IMoreOptionsDialog;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = CreateWorldScreen.class, priority = 1500)
public abstract class CreateWorldScreenMixin extends Screen {
    @Shadow @Final public MoreOptionsDialog moreOptionsDialog;

    protected CreateWorldScreenMixin() {
        super(null);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void interruptCreationMixin(CallbackInfo info) {
        if (!Atum.isRunning()) return;

        if (SeedManager.hasFailed()) {
            MinecraftClient.getInstance().openScreen(new FilterFailedScreen());
            info.cancel();
            return;
        }

        if (SeedManager.canTake()) {
            FSGFilterResult filterResult = SeedManager.take();
            FSGMod.setLastToken(filterResult.token);
            Atum.config.seed = filterResult.seed;
            ((IMoreOptionsDialog) moreOptionsDialog).atum$loadAtumConfigurations();
        } else {
            SeedManager.find();
            MinecraftClient.getInstance().openScreen(new FilteringScreen());
            info.cancel();
        }
    }

    @TargetHandler(
            mixin = "me.voidxwalker.autoreset.mixin.config.CreateWorldScreenMixin",
            name = "modifyAtumCreateWorldScreen"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"), remap = false)
    private void replaceAtumSetSeedLog(Logger instance, String message, Object name, Object seed) {
        instance.info(message.replace("with seed \"{}\"...", "with a filtered seed..."), name);
    }
}
