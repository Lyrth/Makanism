package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import reactor.core.publisher.Mono;

public abstract class Handler {

    Mono<Void> handlers = Mono.empty();

    public static Mono<Void> create(GatewayDiscordClient client) {
        return Mono.empty();
    }

    Mono<Void> handlers(){
        return handlers;
    }

}
