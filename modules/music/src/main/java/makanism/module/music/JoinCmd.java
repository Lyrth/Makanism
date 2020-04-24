package makanism.module.music;

import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(
    name = "Join",
    accessLevel = AccessLevel.OWNER,
    parentModule = Music.class
)
public class JoinCmd extends GuildModuleCommand<Music> {

    @Override
    public Mono<Void> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty() || ctx.getMember().isEmpty()) return Mono.empty();

        return module.join(ctx.getGuildId().get(), ctx.getMember().get())
            .map(b -> b ?
                "joined" :
                "already joined"
            )
            .defaultIfEmpty("you're not in a voice channel")
            .flatMap(reply -> ctx.getChannel().flatMap(ch -> ch.createMessage(reply)).then());
    }
}
