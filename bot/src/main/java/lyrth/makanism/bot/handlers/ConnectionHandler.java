package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class ConnectionHandler extends Handler {
    private static final Logger log = LoggerFactory.getLogger(ConnectionHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        return new ConnectionHandler(client).handlers();
    }

    private ConnectionHandler(GatewayDiscordClient client){    // TODO: modularize?
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down.");
            client.logout().block();
        }));

        handlers = Mono.when(
            ConsoleHandler.create(client),
            ReadyHandler.create(client),
            MessageHandler.create(client)
        );
    }

    public static void register(String a){

    }

}
