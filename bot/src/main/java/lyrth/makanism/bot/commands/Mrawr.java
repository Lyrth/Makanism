package lyrth.makanism.bot.commands;

import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Mrawr extends GuildCommand {

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return ctx.sendReply("mrrawrrf!");
    }
}
