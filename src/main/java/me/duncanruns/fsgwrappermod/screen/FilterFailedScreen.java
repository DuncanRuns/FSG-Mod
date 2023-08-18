package me.duncanruns.fsgwrappermod.screen;

import me.duncanruns.fsgwrappermod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class FilterFailedScreen extends Screen {

    public FilterFailedScreen() {
        super(new LiteralText("Filter failed to run!"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "Check the log for the error.", width / 2, height / 3 + 40, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.CANCEL, buttonWidget -> {
            Atum.isRunning = false;
            SeedManager.acknowledgeFail();
            this.client.openScreen(new TitleScreen());
        }));
    }


}
