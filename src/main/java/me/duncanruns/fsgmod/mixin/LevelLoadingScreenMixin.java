package me.duncanruns.fsgmod.mixin;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.client.gui.screen.LevelLoadingScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = LevelLoadingScreen.class, priority = 1500)
public abstract class LevelLoadingScreenMixin extends Screen {
    protected LevelLoadingScreenMixin(Text title) {
        super(title);
    }

    /**
     * @author tildejustin
     * @author DuncanRuns
     */
    @TargetHandler(
            mixin = "me.voidxwalker.autoreset.mixin.LevelLoadingScreenMixin",
            name = "modifyString"
    )
    @ModifyArg(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/LevelLoadingScreen;drawCenteredString(Lnet/minecraft/client/font/TextRenderer;Ljava/lang/String;III)V"))
    private String replaceAtumRenderText(String ignore) {
        return "Filtered Seed";
    }
}
