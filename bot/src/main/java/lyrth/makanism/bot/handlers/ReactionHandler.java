package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import lyrth.makanism.api.util.MenuRegistry;
import lyrth.makanism.common.util.file.config.BotConfig;
import reactor.core.publisher.Mono;

public class ReactionHandler {

    public static Mono<Void> handle(GatewayDiscordClient client, BotConfig botConfig){
        return Mono.when(
            client.on(ReactionAddEvent.class).flatMap(MenuRegistry::listen),
            client.on(ReactionRemoveEvent.class).flatMap(MenuRegistry::listen)
        );
    }
}
