package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.react.MenuRegistry;
import lyrth.makanism.common.file.config.BotConfig;
import reactor.bool.BooleanUtils;
import reactor.core.publisher.Mono;

public class ReactionHandler {

    public static Mono<?> handle(GatewayDiscordClient client, BotConfig botConfig){
        return Mono.when(
            client.on(ReactionAddEvent.class)
                .filter(e -> !e.getUserId().equals(botConfig.getBotId()))
                .filterWhen(e -> BooleanUtils.not(Mono.justOrEmpty(e.getMember().map(User::isBot)).switchIfEmpty(e.getUser().map(User::isBot))))
                .doOnNext(MenuRegistry::listen),
            client.on(ReactionRemoveEvent.class)
                .filter(e -> !e.getUserId().equals(botConfig.getBotId()))
                .filterWhen(e -> BooleanUtils.not(e.getUser().map(User::isBot)))
                .doOnNext(MenuRegistry::listen)
        );
    }
}
