package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class GatewayHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down.");
            client.logout().block();
        }));

        return Mono.when(
            ReadyHandler.handle(client),
            ConsoleHandler.handle(client),
            CommandHandler.handle(client)
        );
    }

}
