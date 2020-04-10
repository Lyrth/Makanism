package lyrth.makanism.bot.commands;

import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.CommandEvent;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.annotation.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.GENERAL)
public class Ping extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Ping.class);

    @Override
    public Mono<Void> execute(CommandEvent e) {
        String reply = "Mrawr! Arrived **%d**ms late.";
        return e.getChannel()
            .flatMap(ch -> ch.createMessage("Pinging..."))
            .flatMap(msg -> msg.edit(spec -> spec.setContent(
                String.format(reply,
                    msg.getTimestamp().toEpochMilli() - e.getTimestamp().toEpochMilli()))
            ))
            .then();
    }
}

/*
String pong = "Pong! REST: %dms.\nWebsocket: %dms";
return channel.createMessage("Pinging....")
    .flatMap(message -> message.edit(String.format(pong, message.getTimestamp().toEpochMilli() - event.getMessage().getTimestamp().toEpochMilli(), message.getClient().getResponseTime())
  .thenReturn(Reply.empty());
 */