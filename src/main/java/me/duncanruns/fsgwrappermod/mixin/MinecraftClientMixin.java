package me.duncanruns.fsgwrappermod.mixin;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import me.duncanruns.fsgwrappermod.FileUtil;
import me.duncanruns.fsgwrappermod.screen.SuggestDownloadScreen;
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
    private void ensureFSGInstallationMixin(RunArgs args, CallbackInfo info) throws IOException {
        if (!Files.exists(FSGWrapperMod.getFsgDir().resolve(FSGWrapperMod.usingWindows ? "run.bat" : "run.sh"))) {
            boolean seedbankIsPresent = Files.exists(FSGWrapperMod.getFsgDir().resolve("findSeed.py"));

            if (seedbankIsPresent) {
                Path fsgFolderPath = FSGWrapperMod.getFsgDir();
                FileUtil.writeString(fsgFolderPath.resolve("run.bat"), "findSeed");
                Path runShPath = fsgFolderPath.resolve("run.sh");
                FileUtil.writeString(runShPath, "python3 findSeed.py");
                runShPath.toFile().setExecutable(true);

            } else {
                this.openScreen(new SuggestDownloadScreen());
            }
        }
    }
}
