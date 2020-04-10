package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.User;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GuildInitHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildInitHandler.class);

    private static Set<Snowflake> distinctGuilds = new HashSet<>();

    public static Mono<?> handle(GatewayDiscordClient client){
        return client.on(GuildCreateEvent.class)
            .distinct(gce -> gce.getGuild().getId(), GuildInitHandler::getDistinctGuilds)


            .then();
    }

    public static Set<Snowflake> getDistinctGuilds() {
        return distinctGuilds;
    }
}
