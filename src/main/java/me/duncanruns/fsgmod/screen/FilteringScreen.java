package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class FilteringScreen extends AtumWaitingScreen {
    private boolean done = false;
    private boolean failed = false;

    public FilteringScreen() {
        super(new LiteralText("Filtering Seeds..."));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredString(matrices, this.textRenderer, "Filtering Seeds...", width / 2, height / 3, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        new Thread(() -> {
            SeedManager.waitForSeed();
            if (SeedManager.hasSeed()) {
                done = true;
            } else {
                failed = true;
            }
        }).start();
        final int bWidth = 100, bHeight = 20;
        this.addButton(new ButtonWidget(this.width - bWidth, this.height - bHeight, bWidth, bHeight, ScreenTexts.CANCEL, buttonWidget -> cancelWorldCreation()));
    }

    @Override
    public void tick() {
        if (done) continueWorldCreation();
        if (failed) {
            cancelWorldCreation();
            client.openScreen(new FilterFailedScreen());
        }
    }
}
