package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import me.voidxwalker.autoreset.AtumCreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class FilteringScreen extends Screen {
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
        final int bWidth = 100, bHeight = 20;
        this.addButton(new ButtonWidget(this.width - bWidth, this.height - bHeight, bWidth, bHeight, ScreenTexts.CANCEL, buttonWidget -> {
            Atum.stopRunning();
            client.openScreen(null);
        }));
    }

    @Override
    public void tick() {
        if (SeedManager.canTake() || SeedManager.hasFailed()) {
            client.openScreen(new AtumCreateWorldScreen(null));
        }
    }
}
