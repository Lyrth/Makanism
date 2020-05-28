package lyrth.makanism.bot.commands.admin;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    aliases = {"prefix"},
    accessLevel = AccessLevel.OWNER,
    desc = "Sets the server's command prefix.",
    usage = "(<prefix>)"
)
public class SetPrefix extends GuildCommand {       // TODO: rename to prefix, show prefix

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return Mono.just(ctx.getArgs().getRest(1).replace(' ', '_')) // todo not empty
            .doOnNext(prefix -> ctx.getGuildConfig().setPrefix(prefix))
            .map(prefix -> "Server command prefix changed to `" + prefix + "`.")
            .flatMap(ctx::sendReply);
    }
}
