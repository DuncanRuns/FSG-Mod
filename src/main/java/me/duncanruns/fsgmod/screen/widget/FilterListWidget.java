package me.duncanruns.fsgmod.screen.widget;

import me.duncanruns.fsgmod.FSGOnlineDB;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class FilterListWidget extends ElementListWidget<FilterListWidget.FilterEntry> {
    private final int rowWidth;

    private final Set<FSGOnlineDB.FilterInfo> selectedFilters;

    public FilterListWidget(MinecraftClient minecraftClient, int i, int j, int k, int l, List<FSGOnlineDB.FilterInfo> filters, Set<String> selectedFiltersIds) {
        super(minecraftClient, i, j, k, l, 25);
        this.rowWidth = filters.stream().mapToInt(filterInfo -> minecraftClient.textRenderer.getWidth(filterInfo.displayName)).max().orElse(-25) + 25;
        this.selectedFilters = filters.stream().filter(fi -> selectedFiltersIds.contains(fi.id)).collect(Collectors.toCollection(HashSet::new));
        for (FSGOnlineDB.FilterInfo filter : filters) {
            FilterEntry entry = new FilterEntry(filter);
            addEntry(entry);
            if (selectedFilters.contains(filter)) {
                entry.button.setMessage(new LiteralText("✔"));
            }
        }
    }

    @Override
    public int getRowWidth() {
        return rowWidth;
    }

    public Set<FSGOnlineDB.FilterInfo> getSelectedFilters() {
        return selectedFilters;
    }

    public void clearSelectedFilters() {
        selectedFilters.clear();
        children().forEach(entry -> entry.button.setMessage(new LiteralText("")));
    }

    public class FilterEntry extends ElementListWidget.Entry<FilterEntry> {
        private final FSGOnlineDB.FilterInfo filter;
        private final AbstractButtonWidget button;

        public FilterEntry(FSGOnlineDB.FilterInfo filter) {
            this.filter = filter;
            this.button = new ButtonWidget(FilterListWidget.this.rowWidth / 2, 0, 20, 20, new LiteralText(""), b -> {
                if (selectedFilters.contains(filter)) {
                    selectedFilters.remove(filter);
                    b.setMessage(new LiteralText(""));
                } else {
                    selectedFilters.add(filter);
                    b.setMessage(new LiteralText("✔"));
                }
            }) {
                @Override
                public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
                    // The button is also hovered if the entire entry is hovered
                    this.hovered |= FilterListWidget.this.hoveredElement(mouseX, mouseY).orElse(null) == FilterEntry.this;
                    super.renderButton(matrices, mouseX, mouseY, delta);
                }
            };
        }

        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            x = width / 2 - rowWidth / 2;
            button.y = y;
            button.x = x;
            button.render(matrices, mouseX, mouseY, tickDelta);
            int textY = 1 + y + (20 - client.textRenderer.fontHeight) / 2;
            client.textRenderer.drawWithShadow(matrices, filter.displayName, x + 25, textY, 0xFFFFFF);
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
