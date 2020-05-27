package makanism.module.music.commands;

import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.music.Music;
import reactor.core.publisher.Mono;

@CommandInfo(
    name = "Leave",
    accessLevel = AccessLevel.OWNER,
    parentModule = Music.class
)
public class LeaveCmd extends GuildModuleCommand<Music> {

    @Override
    public Mono<?> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty()) return Mono.empty();

        return module.leave(ctx.getGuildId().get())
            .map(b -> b ?
                "disconnected" :
                "already disconnected"
            )
            .flatMap(ctx::sendReply);
    }
}
