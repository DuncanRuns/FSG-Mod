package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGModConfig;
import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class FilteringScreen extends AtumWaitingScreen {
    private boolean done = false;
    private boolean failed = false;
    private final String displayText;

    public FilteringScreen() {
        super(new LiteralText("Filtering Seeds..."));
        displayText = FSGModConfig.getInstance().onlineFilterCode != null ? "Retrieving Seed..." : "Filtering Seeds...";
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredString(matrices, this.textRenderer, displayText, width / 2, height / 3, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        final int bWidth = 100, bHeight = 20;
        this.addButton(new ButtonWidget(this.width - bWidth, this.height - bHeight, bWidth, bHeight, ScreenTexts.CANCEL, buttonWidget -> cancelWorldCreation()));
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
