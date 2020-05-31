package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.config.GuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static java.util.function.Predicate.not;

public class GuildHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildHandler.class);

    public static Mono<?> handle(GatewayDiscordClient client, BotConfig botConfig){
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
            .filter(not(botConfig::hasGuildConfig))   // make sure guildConfig doesn't exist before setting it up
            .flatMap(guildId -> GuildConfig.load(guildId, botConfig.getSourceProvider(), botConfig))
            //.doOnNext(config -> log.debug("Loaded guild config for {}", config.getId().asString()))
            .doOnNext(botConfig::putGuildConfig)
            .doOnNext(guildConfig -> botConfig.getModuleHandler().setupModulesFor(guildConfig))
            .flatMap(GuildConfig::update)
            .then();
    }
}
