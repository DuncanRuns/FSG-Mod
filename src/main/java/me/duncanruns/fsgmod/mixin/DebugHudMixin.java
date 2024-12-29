package me.duncanruns.fsgmod.mixin;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.duck.TokenHolder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.server.integrated.IntegratedServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Mixin(value = DebugHud.class, priority = 1500)
public abstract class DebugHudMixin {
    @Unique
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss");

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void avth(CallbackInfoReturnable<List<String>> info) {
        List<String> list = info.getReturnValue();
        IntegratedServer server = MinecraftClient.getInstance().getServer();
        if (server != null) {
            list.add(String.format("FSG Mod v%s %d", FSGMod.VERSION, Optional.ofNullable(((TokenHolder) server.getSaveProperties()).fsgmod$getToken()).map(String::hashCode).orElse(0)));
        } else {
            list.add(String.format("FSG Mod v%s", FSGMod.VERSION));
        }
        list.add(formatter.format(Instant.now().atZone(ZoneId.of("UTC"))));
    }
}
