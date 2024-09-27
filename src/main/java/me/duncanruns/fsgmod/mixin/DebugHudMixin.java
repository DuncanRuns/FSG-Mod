package me.duncanruns.fsgmod.mixin;

import me.duncanruns.fsgmod.FSGMod;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Mixin(value = DebugHud.class, priority = 1500)
public abstract class DebugHudMixin {
    @Unique
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd MMM uuuu HH:mm:ss");

    @Inject(method = "getRightText", at = @At("RETURN"))
    private void avth(CallbackInfoReturnable<List<String>> info) {
        List<String> list = info.getReturnValue();
        list.add(String.format("FSGWM v%s %d", FSGMod.VERSION, FSGMod.lastTokenHash));
        list.add(formatter.format(Instant.now().atZone(ZoneId.of("UTC"))));
    }
}
