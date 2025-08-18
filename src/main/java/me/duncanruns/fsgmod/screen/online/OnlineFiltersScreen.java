package me.duncanruns.fsgmod.screen.online;

import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.FSGOnlineDB;
import me.duncanruns.fsgmod.screen.ConfigScreen;
import me.duncanruns.fsgmod.screen.DocumentScreen;
import me.duncanruns.fsgmod.screen.local.LoadingLocalFiltersScreen;
import me.duncanruns.fsgmod.screen.local.LocalFiltersScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class OnlineFiltersScreen extends Screen {
    private OnlineFilterListWidget filterListWidget;
    private final List<FSGOnlineDB.FilterInfo> filters;
    private final Set<String> initiallySelectedFilters;
    private boolean practiceMode;
    private ButtonWidget confirmButton;


    public OnlineFiltersScreen(List<FSGOnlineDB.FilterInfo> filters) {
        this(filters, FSGModConfig.getInstance().selectedOnlineFilters, FSGModConfig.getInstance().practiceMode);
    }

    public OnlineFiltersScreen(List<FSGOnlineDB.FilterInfo> filters, Set<String> selectedFiltersIds, boolean practiceMode) {
        super(new LiteralText("FSG Mod: Select Online Filter(s)"));
        this.filters = filters;
        this.initiallySelectedFilters = selectedFiltersIds;
        this.practiceMode = practiceMode;
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.filterListWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, 15, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "Select the filters you want to use.", width / 2, 30, 0xFFFFFF);
    }

    @Override
    protected void init() {
        assert client != null;
        if (this.filterListWidget != null) {
            this.filterListWidget = new OnlineFilterListWidget(client, width, height, 45, this.height - 88, filters, getSelectedFilterIds());
        } else {
            this.filterListWidget = new OnlineFilterListWidget(client, width, height, 45, this.height - 88, filters, initiallySelectedFilters);
        }
        addChild(filterListWidget);

        // Confirm button
        this.confirmButton = addButton(new ButtonWidget(
                this.width / 2 - 153, this.height - 76, 150, 20,
                new LiteralText("Confirm Selection"),
                buttonWidget -> this.confirm()
        ));
        updateConfirmButton();

        // Clear selection button
        addButton(new ButtonWidget(
                this.width / 2 + 3, this.height - 76, 150, 20,
                new LiteralText("Clear Selection"),
                buttonWidget -> {
                    filterListWidget.clearSelectedFilters();
                    updateConfirmButton();
                }
        ));

        // Practice mode button
        addButton(new ButtonWidget(
                this.width / 2 - 153, this.height - 52, 150, 20,
                new LiteralText("Practice Mode: " + (practiceMode ? "ON" : "OFF")),
                buttonWidget -> {
                    practiceMode = !practiceMode;
                    buttonWidget.setMessage(new LiteralText("Practice Mode: " + (practiceMode ? "ON" : "OFF")));
                },
                (button, matrices, mouseX, mouseY) -> {
                    renderTooltip(matrices, new LiteralText("Practice Mode gives seeds that were previously used by other players, and "), mouseX, mouseY);
                }
        ));

        // Info button
        this.addButton(new ButtonWidget(
                this.width / 2 + 3, this.height - 52, 150, 20,
                new LiteralText("ⓘ Filter Info"),
                buttonWidget -> client.openScreen(new LoadingInfoDocScreen(string -> client.openScreen(new DocumentScreen(new LiteralText("Filters Info"), string, () -> this.client.openScreen(
                        new LoadingOnlineFiltersScreen(filterInfos -> client.openScreen(
                                new OnlineFiltersScreen(filterInfos, getSelectedFilterIds(), practiceMode)
                        ))
                )))))
        ));

        // Local filters button
        Util.OperatingSystem os = Util.getOperatingSystem();
        addButton(new ButtonWidget(
                this.width / 2 - 153, this.height - 28, 150, 20,
                new LiteralText("Install a local filter...").formatted(Formatting.RED),
                b -> client.openScreen(new LoadingLocalFiltersScreen(infos -> client.openScreen(new LocalFiltersScreen(infos))))
        )).active = (os == Util.OperatingSystem.WINDOWS || os == Util.OperatingSystem.LINUX || os == Util.OperatingSystem.OSX);

        // Refresh button
        this.addButton(new ButtonWidget(
                this.width / 2 + 3, this.height - 28, 72, 20,
                new TranslatableText("selectServer.refresh"),
                buttonWidget -> refresh()
        ));

        // Cancel button
        this.addButton(new ButtonWidget(
                this.width / 2 + 3 + 72 + 6, this.height - 28, 72, 20,
                ScreenTexts.CANCEL,
                buttonWidget -> client.openScreen(new ConfigScreen())
        ));
    }

    private void refresh() {
        assert client != null;
        client.openScreen(new LoadingOnlineFiltersScreen(
                filterInfos -> client.openScreen(new OnlineFiltersScreen(filterInfos, getSelectedFilterIds(), practiceMode)),
                true
        ));
    }

    private @NotNull Set<String> getSelectedFilterIds() {
        return filterListWidget.getSelectedFilters().stream().map(fi -> fi.id).collect(Collectors.toSet());
    }

    private void confirm() {
        assert client != null;
        FSGModConfig config = FSGModConfig.getInstance();
        Set<String> selectedFilterIds = getSelectedFilterIds();
        config.selectedOnlineFilters = selectedFilterIds;
        if (selectedFilterIds.size() == 1) {
            config.selectedOnlineFilterName = filters.stream().filter(fi -> fi.id.equals(selectedFilterIds.iterator().next())).findFirst().get().displayName;
        } else {
            config.selectedOnlineFilterName = null;
        }
        config.practiceMode = practiceMode;
        client.openScreen(new ConfigScreen());
        FSGModConfig.trySave();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean out = super.mouseClicked(mouseX, mouseY, button);
        if (out) {
            updateConfirmButton();
            return true;
        }
        return false;
    }

    private void updateConfirmButton() {
        this.confirmButton.active = !filterListWidget.getSelectedFilters().isEmpty();
    }
}
