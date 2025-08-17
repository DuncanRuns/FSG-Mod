package me.duncanruns.fsgmod.screen.online;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGOnlineDB;
import me.duncanruns.fsgmod.screen.SimpleTextScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.function.Consumer;

public class LoadingInfoDocScreen extends Screen {
    private String doc = null;
    private boolean loaded = false;
    private boolean failed = false;
    private final Consumer<String> onLoaded;

    protected LoadingInfoDocScreen(Consumer<String> onLoaded) {
        super(new LiteralText("Retrieving Info Document..."));
        this.onLoaded = onLoaded;

        FSGOnlineDB.getFilterInfoDoc().handle((string, throwable) -> {
            if (throwable != null) {
                FSGMod.LOGGER.error("Failed to load info doc!", throwable);
                failed = true;
                return null;
            }
            doc = string;
            loaded = true;
            return null;
        });
    }

    @Override
    public void tick() {
        assert client != null;
        if (loaded) {
            onLoaded.accept(doc);
        } else if (failed) {
            client.openScreen(new SimpleTextScreen(new LiteralText("Failed to load info doc!"), "Please check the log for more information.", true));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
