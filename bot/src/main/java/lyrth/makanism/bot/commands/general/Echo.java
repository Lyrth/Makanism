package lyrth.makanism.bot.commands.general;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Repeats what it is told to.",
    usage = "[<text>...]"
)
public class Echo extends GuildCommand {

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return ctx.sendReply(ctx.getArgs().getRest(1));
    }
}
