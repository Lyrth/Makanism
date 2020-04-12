package lyrth.makanism.bot.commands;

import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class SetPrefix extends GuildCommand {
    @Override
    public Mono<Void> execute(CommandCtx e) {

        return Mono.just(e.getArgs().getRest(1).replace(' ', '_'))
            .doOnNext(prefix -> e.getGuildConfig().setPrefix(prefix))
            .map(prefix -> "Server command prefix changed to `" + prefix + "`.")
            .flatMap(reply ->
                e.getChannel().flatMap(ch -> ch.createMessage(reply))
            ).then();
    }
}
