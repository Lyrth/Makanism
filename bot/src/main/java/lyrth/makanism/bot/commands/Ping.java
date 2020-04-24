package lyrth.makanism.bot.commands;

import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.annotation.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.GENERAL)
public class Ping extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Ping.class);

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        String reply = "Mrawr! Arrived **%d**ms late.";
        return ctx.getChannel()
            .flatMap(ch -> ch.createMessage("Pinging..."))
            .flatMap(msg -> msg.edit(spec -> spec.setContent(
                String.format(reply,
                    msg.getTimestamp().toEpochMilli() - ctx.getTimestamp().toEpochMilli()))
            ))
            .then();
    }
}