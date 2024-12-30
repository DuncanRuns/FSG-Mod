package me.duncanruns.fsgmod;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import me.duncanruns.fsgmod.mixinint.TokenHolder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

public class TokenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("fsgtoken").executes(TokenCommand::execute));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        String token = ((TokenHolder) context.getSource().getMinecraftServer().getSaveProperties()).fsgmod$getToken();
        if (token == null) {
            context.getSource().sendError(new LiteralText("This world does not have a token."));
            return 0;
        }
        context.getSource().sendFeedback(
                new LiteralText("Last token: ").append(
                        Texts.bracketed(new LiteralText(token).styled(style -> style.withColor(Formatting.GREEN)
                                .withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, token))
                                .setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableText("chat.copy.click")))))
                ), false
        );
        return 1;
    }
}
