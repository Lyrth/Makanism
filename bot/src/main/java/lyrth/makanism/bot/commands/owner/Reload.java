package lyrth.makanism.bot.commands.owner;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Reload modules."
)
public class Reload extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Reload.class);

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return ctx.getBotConfig().getModuleHandler().unload()
            .thenReturn("Unloaded jars! Waiting for `.ok`")
            .flatMap(ctx::sendReply)

            .then(ctx.getClient().on(MessageCreateEvent.class)
                .filter(m ->
                    m.getMessage().getContent().equals(".ok") &&
                    m.getMessage().getChannelId().equals(ctx.getChannelId()) &&
                    m.getMessage().getAuthor().map(User::getId).equals(ctx.getAuthorId()))
                .next()
            )
            .thenReturn("Reloaded.")
            .flatMap(ctx::sendReply)
            .then(Mono.defer(() ->
                ctx.getBotConfig().getModuleHandler().reload(ctx.getClient(), ctx.getBotConfig())));
    }
}
