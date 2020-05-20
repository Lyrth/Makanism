package lyrth.makanism.bot.commands;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.util.MenuMessage;
import lyrth.makanism.api.util.YesNoReactListener;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Test extends GuildCommand {

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        return MenuMessage.create("testing", new YesNoReactListener()).send(ctx).then();
    }
}
