package me.duncanruns.fsgmod.mixin;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Inject(method = "startIntegratedServer(Ljava/lang/String;Lnet/minecraft/util/registry/RegistryTracker$Modifiable;Ljava/util/function/Function;Lcom/mojang/datafixers/util/Function4;ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V", at = @At("TAIL"))
    private void onFinishCreateWorld(CallbackInfo info) {
        if (Atum.isRunning() && FSGMod.shouldRunInBackground()) {
            SeedManager.find();
        }
    }
}
