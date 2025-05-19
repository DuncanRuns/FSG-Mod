package me.duncanruns.fsgmod.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import java.util.function.Consumer;
import java.util.function.Function;

public class SimpleTextScreen extends Screen {
    private final String text;
    private final boolean exitable;
    private final Function<SimpleTextScreen, ButtonWidget> buttonProvider;

    public SimpleTextScreen(Text title, String text, boolean exitable) {
        this(title, text, exitable, null);
    }

    public SimpleTextScreen(Text title, String text, boolean exitable, Function<SimpleTextScreen, ButtonWidget> buttonProvider) {
        super(title);
        this.text = text;
        this.exitable = exitable;
        this.buttonProvider = buttonProvider;
    }

    @Override
    protected void init() {
        if(buttonProvider != null) {
            this.addButton(buttonProvider.apply(this));
        }
        if (exitable)
            this.addButton(new ButtonWidget(this.width / 2 - 100, this.height / 6 + 168, 200, 20, ScreenTexts.CANCEL, buttonWidget -> this.client.openScreen(new TitleScreen())));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, text, width / 2, height / 3 + 20, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return exitable;
    }
}
