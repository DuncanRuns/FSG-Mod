package me.duncanruns.fsgwrappermod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

public class BackgroundCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fsgbackground").executes(BackgroundCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        context.getSource().sendFeedback(new LiteralText((FSGWrapperMod.toggleRunInBackground() ? "Enabled" : "Disabled") + " background filtering"), false);
        return 1;
    }
}
