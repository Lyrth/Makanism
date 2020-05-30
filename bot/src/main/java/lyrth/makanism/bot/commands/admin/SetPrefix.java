package lyrth.makanism.bot.commands.admin;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    aliases = {"prefix"},
    accessLevel = AccessLevel.OWNER,
    desc = "Set or get the server's command prefix.",
    usage = "[<prefix>]"
)
public class SetPrefix extends GuildCommand {

    private static final String GET_PREFIX_MSG = "The server prefix is `%s`.";
    private static final String SET_PREFIX_MSG = "Server command prefix changed to `%s`.";


    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().isEmpty()){
            return ctx.sendReply(String.format(GET_PREFIX_MSG, ctx.getGuildConfig().getPrefix()));
        }

        String newPrefix = ctx.getArgs().getRest(1).replace(' ', '_');
        ctx.getGuildConfig().setPrefix(newPrefix);

        return ctx.sendReply(String.format(SET_PREFIX_MSG, newPrefix));
    }
}
