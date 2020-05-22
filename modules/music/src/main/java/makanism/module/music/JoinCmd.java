package makanism.module.music;

import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
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
    public Mono<?> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty() || ctx.getMember().isEmpty()) return Mono.empty();

        return module.join(ctx.getGuildId().get(), ctx.getMember().get())
            .map(b -> b ?
                "joined" :
                "already joined"
            )
            .defaultIfEmpty("you're not in a voice channel")
            .flatMap(ctx::sendReply);
    }
}
