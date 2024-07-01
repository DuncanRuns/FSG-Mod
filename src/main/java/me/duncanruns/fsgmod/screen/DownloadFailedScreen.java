package me.duncanruns.fsgmod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;

public class DownloadFailedScreen extends Screen {
    public DownloadFailedScreen() {
        super(new LiteralText("Download Failed!"));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        this.drawCenteredString(minecraft.textRenderer, this.title.asString(), width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(minecraft.textRenderer, "Please close Minecraft and report the error in the log.", width / 2, height / 3 + 20, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
