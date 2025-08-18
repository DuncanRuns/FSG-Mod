package me.duncanruns.fsgmod.screen.local;

import me.duncanruns.fsgmod.FSGModConfig;
import me.duncanruns.fsgmod.FSGModMeta;
import me.duncanruns.fsgmod.screen.ConfigScreen;
import me.duncanruns.fsgmod.screen.online.LoadingOnlineFiltersScreen;
import me.duncanruns.fsgmod.screen.online.OnlineFiltersScreen;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;

import java.util.List;
import java.util.stream.Collectors;

public class LocalFiltersScreen extends Screen {
    private LocalFilterListWidget filterListWidget;
    private final List<FSGModMeta.FilterInfo> filters;
    private final String minecraftVersion = SharedConstants.getGameVersion().getName();


    public LocalFiltersScreen(List<FSGModMeta.FilterInfo> filters) {
        super(new LiteralText("FSG Mod: Select Local Filter"));
        this.filters = filters.stream().filter(filterInfo -> filterInfo.supportedVersions.contains(minecraftVersion)).collect(Collectors.toList());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        renderBackground(matrices);
        this.filterListWidget.render(matrices, mouseX, mouseY, delta);
        super.render(matrices, mouseX, mouseY, delta);
        this.drawCenteredText(matrices, this.textRenderer, this.title, width / 2, 15, 0xFFFFFF);
        this.drawCenteredString(matrices, this.textRenderer, "Select the filter you want to use.", width / 2, 30, 0xFFFFFF);
    }

    @Override
    protected void init() {
        assert client != null;
        this.filterListWidget = new LocalFilterListWidget(client, width, height, 45, this.height - 40, filters);
        addChild(filterListWidget);

        // Online filters button
        Util.OperatingSystem os = Util.getOperatingSystem();
        addButton(new ButtonWidget(
                this.width / 2 - 153, this.height - 28, 150, 20,
                new LiteralText("Install an online filter..."),
                b -> client.openScreen(
                        new LoadingOnlineFiltersScreen(filterInfos ->
                                client.openScreen(new OnlineFiltersScreen(filterInfos)))
                )
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
        client.openScreen(new LoadingLocalFiltersScreen(
                infos -> client.openScreen(new LocalFiltersScreen(infos)),
                true
        ));
    }
}
