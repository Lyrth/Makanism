package makanism.module.music;

import lyrth.makanism.api.*;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    name = "Play",
    accessLevel = AccessLevel.OWNER,
    parentModule = Music.class
)
public class PlayCmd extends GuildModuleCommand<Music> {

    @Override
    public Mono<Void> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty() || ctx.getMember().isEmpty()) return Mono.empty();
        if (ctx.getArgs().getRest(1).isEmpty())
            return ctx.getChannel().flatMap(ch -> ch.createMessage("Invalid args.")).then();

        return module.play(ctx.getGuildId().get(),ctx.getArgs().getRest(1),ctx.getMember().get())
            .map(b -> b ?
                "playing" :
                "invalid music"
            )
            .defaultIfEmpty("you're not in a voice channel")
            .flatMap(reply -> ctx.getChannel().flatMap(ch -> ch.createMessage(reply)).then());
    }
}
