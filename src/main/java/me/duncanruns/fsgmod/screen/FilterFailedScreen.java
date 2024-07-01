package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.SeedManager;
import me.voidxwalker.autoreset.Atum;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;

import java.nio.file.Files;

public class FilterFailedScreen extends Screen {
    private String text = "Check the log for the error.";

    public FilterFailedScreen() {
        super(new LiteralText("Filter failed to run!"));
    }

    @Override
    public void render(int mouseX, int mouseY, float delta) {
        renderBackground();
        this.drawCenteredString(minecraft.textRenderer, this.title.asString(), width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(minecraft.textRenderer, text, width / 2, height / 3 + 40, 0xFFFFFF);
        super.render(mouseX, mouseY, delta);
    }

    @Override
    protected void init() {
        if (!Files.exists(FSGMod.getFsgDir())) {
            text = "No filter is installed! Go to the FSG mod options (wheat seeds button).";
        }
        this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, I18n.translate("gui.cancel"), buttonWidget -> {
            Atum.isRunning = false;
            SeedManager.acknowledgeFail();
            this.minecraft.openScreen(new TitleScreen());
        }));
    }


}
