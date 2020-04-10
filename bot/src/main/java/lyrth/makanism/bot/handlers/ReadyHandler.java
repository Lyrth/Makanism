package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Set;
import java.util.stream.Collectors;

public class ReadyHandler {
    private static final Logger log = LoggerFactory.getLogger(ReadyHandler.class);

    public static Mono<User> handle(GatewayDiscordClient client){
        return client.on(ReadyEvent.class).next()
            .doOnNext(ready -> log.info("Logged in as {}#{}", ready.getSelf().getUsername(), ready.getSelf().getDiscriminator()))
            .doOnNext(ready -> log.info("Shard [#{} total], in {} guilds", ready.getShardInfo().format(), ready.getGuilds().size()))
            .map(ReadyEvent::getSelf);
    }

}
