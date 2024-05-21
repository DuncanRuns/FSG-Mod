package me.duncanruns.fsgwrappermod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class SuggestDownloadScreen extends Screen {
    public SuggestDownloadScreen() {
        super(new LiteralText("FSG Download Suggestion"));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredString(matrices, this.textRenderer, "Do you want to download RSGButGood?", width / 2, height / 3, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        this.addButton(new ButtonWidget(this.width / 2 - 155, this.height / 6 + 96, 150, 20, ScreenTexts.YES, buttonWidget -> {
            client.openScreen(new DownloadingRSGButGoodScreen());
        }));
        this.addButton(new ButtonWidget(this.width / 2 - 155 + 160, this.height / 6 + 96, 150, 20, ScreenTexts.NO, buttonWidget -> {
            client.openScreen(new NoFSGScreen());
        }));
    }
}
