package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ReadyHandler extends Handler {
    private static final Logger log = LoggerFactory.getLogger(ReadyHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        return new ReadyHandler(client).handlers();
    }

    private ReadyHandler(GatewayDiscordClient client){  // TODO: Guild setup
        handlers = client.on(ReadyEvent.class)
            .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
            .then();
    }
}
