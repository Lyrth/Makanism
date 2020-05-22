package lyrth.makanism.bot.commands;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.util.*;
import lyrth.makanism.api.util.buttons.MenuReactListener;
import lyrth.makanism.api.util.buttons.ReactListener;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Test extends GuildCommand {

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        ReactListener menuListener = new MenuReactListener()
            .addAction("one", e -> ctx.sendReply("It's a one!"))
            .addAction("two", e -> ctx.sendReply("It's a two!"))
            .addAction("six", e -> ctx.sendReply("It's a six!"))
            .addAction("four", e -> ctx.sendReply("It's a four!"))
            .cancelOn("x")
            .addAction("b", e -> ctx.sendReply("It's a b!"));
        return MenuMessage.create("1264xb", menuListener).send(ctx).then();
    }
}
