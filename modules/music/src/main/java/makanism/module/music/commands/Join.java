package makanism.module.music.commands;

import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.music.Music;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Joins the voice channel the invoker member is in."
)
public class Join extends GuildModuleCommand<Music> {

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
