package lyrth.makanism.api;

import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.util.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo()
public abstract class GuildModuleCommand<M extends GuildModule> extends GuildCommand {

    public Mono<Void> execute(CommandCtx ctx) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Don't use this.");
    }

    public abstract Mono<Void> execute(CommandCtx ctx, M module);
}
