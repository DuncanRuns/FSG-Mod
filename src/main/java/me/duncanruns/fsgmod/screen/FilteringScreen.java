package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;

public class FilteringScreen extends Screen {
    public FilteringScreen() {
        super(new LiteralText("Filtering Seeds..."));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        this.drawCenteredString(minecraft.textRenderer, "Filtering Seeds...", width / 2, height / 3, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        final int bWidth = 100, bHeight = 20;
        this.addButton(new ButtonWidget(this.width - bWidth, this.height - bHeight, bWidth, bHeight, I18n.translate("gui.cancel"), buttonWidget -> {
            Atum.isRunning = false;
            minecraft.openScreen(null);
        }));
    }

    @Override
    public void tick() {
        if (SeedManager.canTake() || SeedManager.hasFailed()) {
            minecraft.openScreen(new CreateWorldScreen(null));
        }
    }
}
