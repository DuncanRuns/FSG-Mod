package me.duncanruns.fsgwrappermod.mixin;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import me.duncanruns.fsgwrappermod.FileUtil;
import me.duncanruns.fsgwrappermod.SeedManager;
import me.duncanruns.fsgwrappermod.screen.SuggestDownloadScreen;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.gui.screen.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow
    public abstract void openScreen(@Nullable Screen screen);

    @Inject(method = "<init>", at = @At("TAIL"))
    private void ensureFSGInstallationMixin(RunArgs args, CallbackInfo info) {
        if (!Files.exists(FSGWrapperMod.getRunPath())) {
            boolean filterIsPresent = Files.exists(FSGWrapperMod.getRunPath());

            if (!filterIsPresent) {
                this.openScreen(new SuggestDownloadScreen());
            }
        }
    }

    @Inject(method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V", at = @At("TAIL"))
    private void onFinishCreateWorld(CallbackInfo info) {
        if (Atum.isRunning && FSGWrapperMod.shouldRunInBackground()) {
            SeedManager.find();
        }
    }
}
