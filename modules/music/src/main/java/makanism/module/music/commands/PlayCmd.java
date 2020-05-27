package makanism.module.music.commands;

import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import makanism.module.music.Music;
import reactor.core.publisher.Mono;

@CommandInfo(
    name = "Play",
    accessLevel = AccessLevel.OWNER,
    parentModule = Music.class
)
public class PlayCmd extends GuildModuleCommand<Music> {

    @Override
    public Mono<?> execute(CommandCtx ctx, Music module) {
        if (ctx.getGuildId().isEmpty() || ctx.getMember().isEmpty()) return Mono.empty();
        if (ctx.getArgs().getRest(1).isEmpty())
            return ctx.getChannel().flatMap(ch -> ch.createMessage("Invalid args.")).then();

        return module.play(ctx.getGuildId().get(),ctx.getArgs().getRest(1),ctx.getMember().get())
            .map(b -> b ?
                "playing" :
                "invalid music"
            )
            .defaultIfEmpty("you're not in a voice channel")
            .flatMap(ctx::sendReply);
    }
}
