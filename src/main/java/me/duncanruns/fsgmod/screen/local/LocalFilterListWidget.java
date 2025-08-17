package me.duncanruns.fsgmod.screen.local;

import me.duncanruns.fsgmod.FSGMod;
import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.FSGModMeta;
import me.duncanruns.fsgmod.LocalFilter;
import me.duncanruns.fsgmod.screen.ConfigScreen;
import me.duncanruns.fsgmod.util.FileUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

public class LocalFilterListWidget extends ElementListWidget<LocalFilterListWidget.FilterEntry> {
    private final int rowWidth;
    private final boolean noFilters;

    public LocalFilterListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, List<FSGModMeta.FilterInfo> filters) {
        super(minecraftClient, i, j, k, l, 25);
        int rowWidth;
        rowWidth = filters.stream().mapToInt(filterInfo -> minecraftClient.textRenderer.getWidth(filterInfo.getDisplayName())).max().orElse(-10) + 10;
        if (rowWidth % 2 == 1) rowWidth++;
        this.rowWidth = rowWidth;
        for (FSGModMeta.FilterInfo filter : filters) {
            FilterEntry entry = new FilterEntry(filter);
            addEntry(entry);
        }
        this.noFilters = filters.isEmpty();
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);
        if (noFilters) {
            drawCenteredString(matrices, client.textRenderer, "No filters are available for this version of Minecraft.", width / 2, height / 2, 0xFFFFFF);
        }

    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public class FilterEntry extends ElementListWidget.Entry<FilterEntry> {
        private final AbstractButtonWidget button;

        public FilterEntry(FSGModMeta.FilterInfo filter) {
            this.button = new ButtonWidget(LocalFilterListWidget.this.rowWidth / 2, 0, rowWidth, 20, new LiteralText(filter.getDisplayName()), b -> client.openScreen(
                    new DownloadingScreen(FSGModMeta.getDownloadUrl(filter), new ConfigScreen(), () -> {
                        try {
                            if (filter.runBatScript != null) {
                                FileUtil.writeString(LocalFilter.getFsgDir().resolve("run.bat"), filter.runBatScript);
                            }
                            if (filter.runShScript != null) {
                                FileUtil.writeString(LocalFilter.getFsgDir().resolve("run.sh"), filter.runShScript);
                            }
                            FSGMod.setAllInFolderExecutable();
                            FSGModConfig config = FSGModConfig.getInstance();
                            config.selectedOnlineFilters = new HashSet<>();
                            config.selectedOnlineFilterName = null;
                            FSGModConfig.trySave();
                            LocalFilter.writeData(filter.maxGenerating, filter.name, filter.runIsRetimed);
                        } catch (IOException e) {
                            FSGMod.logError("Failed to install filter!", e);
                        }
                    })
            ));
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            x = width / 2 - rowWidth / 2;
            button.y = y;
            button.x = x;
            button.render(matrices, mouseX, mouseY, tickDelta);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            return this.button.mouseClicked(this.button.x, this.button.y, button);
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(button);
        }
    }
}
