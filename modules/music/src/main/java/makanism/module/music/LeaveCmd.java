package makanism.module.music;

import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(
    name = "Leave",
    accessLevel = AccessLevel.OWNER,
    parentModule = Music.class
)
public class LeaveCmd extends GuildModuleCommand<Music> {

    @Override
    public Mono<Void> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty()) return Mono.empty();

        return module.leave(ctx.getGuildId().get())
            .map(b -> b ?
                "disconnected" :
                "already disconnected"
            )
            .flatMap(reply -> ctx.getChannel().flatMap(ch -> ch.createMessage(reply)).then());
    }
}
