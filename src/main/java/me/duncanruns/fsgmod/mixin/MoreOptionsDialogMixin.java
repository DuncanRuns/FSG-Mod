package me.duncanruns.fsgmod.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.world.MoreOptionsDialog;
import org.apache.logging.log4j.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = MoreOptionsDialog.class, priority = 1500)
public class MoreOptionsDialogMixin {

    @TargetHandler(
            mixin = "me.voidxwalker.autoreset.mixin.MoreOptionsDialogMixin",
            name = "setSeed"
    )
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lme/voidxwalker/autoreset/Atum;log(Lorg/apache/logging/log4j/Level;Ljava/lang/String;)V"))
    private void replaceAtumLog(Level level, String message) {
        Atum.log(level, message.startsWith("Resetting the set seed") ? "Resetting a filtered seed" : message);
    }
}
