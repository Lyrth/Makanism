package lyrth.makanism.bot.commands;

import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Echo extends GuildCommand {

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        return Mono.just(ctx.getArgs().getRest(1).replace('@', '%'))
            .flatMap(reply ->
                ctx.getChannel().flatMap(ch -> ch.createMessage(reply))
            ).then();
    }
}
