package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.common.util.file.SourceProvider;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.config.GuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;

public class GuildHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildHandler.class);

    private static Set<Snowflake> distinctGuilds = new HashSet<>();  // static???

    public static Mono<Void> handle(GatewayDiscordClient client, BotConfig botConfig, SourceProvider source, BotProps props){
        return client.on(GuildCreateEvent.class)
            // No need for distinct, just make sure guild settings can be "reloaded"
            //.distinct(gce -> gce.getGuild().getId(), GuildHandler::getDistinctGuilds)
            .doOnNext(gce -> log.info("[Shard {}] Discovered guild \"{}\" ({}) with {} members.",
                gce.getShardInfo().getIndex(),
                gce.getGuild().getName(),
                gce.getGuild().getId().asString(),
                gce.getGuild().getMemberCount()
            ))
            .map(gce -> gce.getGuild().getId())
            .flatMap(guildId -> GuildConfig.load(guildId, source, props))
            .flatMap(GuildConfig::update)
            //.doOnNext(config -> log.debug("Loaded guild config for {}", config.getId().asString()))
            .map(botConfig::putGuildConfig)
            .then();
    }

    public static Set<Snowflake> getDistinctGuilds() {
        return distinctGuilds;
    }
}
