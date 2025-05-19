package me.duncanruns.fsgmod.screen;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGOnlineDB;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import java.util.List;
import java.util.function.Consumer;

public class LoadingFiltersScreen extends Screen {
    private final Consumer<List<FSGOnlineDB.FilterInfo>> onLoaded;
    private List<FSGOnlineDB.FilterInfo> filters = null;
    private boolean loaded = false;
    private boolean failed = false;

    public LoadingFiltersScreen(Consumer<List<FSGOnlineDB.FilterInfo>> onLoaded) {
        this(onLoaded, false);
    }

    public LoadingFiltersScreen(Consumer<List<FSGOnlineDB.FilterInfo>> onLoaded, boolean refresh) {
        super(new LiteralText("Loading Filters..."));
        this.onLoaded = onLoaded;

        FSGOnlineDB.getFilters(refresh).handle((filterInfos, throwable) -> {
            if (throwable != null) {
                FSGMod.LOGGER.error("Failed to load filters!", throwable);
                failed = true;
                return null;
            }
            filters = filterInfos;
            loaded = true;
            return null;
        });
    }

    @Override
    public void tick() {
        assert client != null;
        if (loaded) {
            onLoaded.accept(filters);
        } else if (failed) {
            client.openScreen(new SimpleTextScreen(new LiteralText("Failed to load filters!"), "Please check the log for more information.", true, s -> {
                // Local filters screen
                return new ButtonWidget(s.width / 2 - 100, s.height / 6 + 140, 200, 20, new LiteralText("Install a local filter instead...").formatted(Formatting.RED), b ->
                        client.openScreen(new LocalFiltersScreen())
                );
            }));
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, height / 3, 0xFFFFFF);
        super.render(matrices, mouseX, mouseY, delta);
    }
}
