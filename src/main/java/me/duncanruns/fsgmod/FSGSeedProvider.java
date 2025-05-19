package me.duncanruns.fsgmod;

import me.duncanruns.fsgmod.screen.FilteringScreen;
import me.duncanruns.fsgmod.screen.SimpleTextScreen;
import me.voidxwalker.autoreset.api.seedprovider.AtumWaitingScreen;
import me.voidxwalker.autoreset.api.seedprovider.SeedProvider;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

public class FSGSeedProvider implements SeedProvider {

    @Override
    public CompletableFuture<String> requestSeed() {
        CompletableFuture<String> sf = new CompletableFuture<>();
        SeedManager.requestSeed(MinecraftClient.getInstance().isOnThread(), sf);
        return sf;
    }


    @Override
    public boolean shouldShowSeed() {
        return false;
    }

    @Override
    public Optional<AtumWaitingScreen> getWaitingScreen() {
        return Optional.of(new FilteringScreen());
    }

    @Override
    public void onFail(@Nullable Throwable ex) {
        if (ex instanceof CancellationException) return;

        MinecraftClient client = MinecraftClient.getInstance();

        String text = "Check the log for the error.";
        if (!FSGMod.filterSelectedOrInstalled()) {
            text = "No filter is selected! Go to the FSG mod options (wheat seeds button).";
        } else if (!LocalFilter.isInstalled()) {
            text = "Online filter failed! The server might be down or the filter might be unavailable.";
        }
        if (client.world != null && client.player != null) {
            client.inGameHud.getChatHud().addMessage(new LiteralText("(FSG Mod) Filtering has failed!").copy().styled(style -> style.withColor(Formatting.RED).withColor(Formatting.BOLD)));
        } else {
            client.openScreen(new SimpleTextScreen(new LiteralText("Filter failed to run!"), text, true));
        }
    }
}
