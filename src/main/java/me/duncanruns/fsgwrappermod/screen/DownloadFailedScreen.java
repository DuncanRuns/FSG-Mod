package me.duncanruns.fsgwrappermod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class DownloadFailedScreen extends Screen {
    public DownloadFailedScreen() {
        super(new LiteralText("Download Failed!"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "Please close Minecraft and report the error in the log.", width / 2, height / 3 + 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
