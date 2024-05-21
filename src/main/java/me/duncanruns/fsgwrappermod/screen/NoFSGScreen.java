package me.duncanruns.fsgwrappermod.screen;

import me.duncanruns.fsgwrappermod.FSGWrapperMod;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class NoFSGScreen extends Screen {
    public NoFSGScreen() {
        super(new LiteralText("No fsg detected!"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredString(matrices, this.textRenderer, "You don't have fsg setup!", width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "Please create /fsg/run" + (FSGWrapperMod.USING_WINDOWS ? ".bat" : ".sh") + " with", width / 2, height / 3 + 20, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "the command to run a filter.", width / 2, height / 3 + 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
