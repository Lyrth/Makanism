package lyrth.makanism.bot.commands;

import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class SetPrefix extends GuildCommand {

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        return Mono.just(ctx.getArgs().getRest(1).replace(' ', '_')) // todo not empty
            .doOnNext(prefix -> ctx.getGuildConfig().setPrefix(prefix))
            .map(prefix -> "Server command prefix changed to `" + prefix + "`.")
            .flatMap(ctx::sendReply);
    }
}
