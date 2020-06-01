package lyrth.makanism.bot.handlers;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.config.GuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static reactor.function.TupleUtils.function;
import static reactor.function.TupleUtils.predicate;

public class GuildHandler {
    private static final Logger log = LoggerFactory.getLogger(GuildHandler.class);

    public static Mono<?> handle(EventDispatcher dispatcher, Mono<BotConfig> botConfig){
        Flux<BotConfig> botConfigSource = botConfig.repeat();
        return dispatcher.on(GuildCreateEvent.class)
            // No need for distinct, just make sure guild settings can be "reloaded"
            //.distinct(gce -> gce.getGuild().getId(), GuildHandler::getDistinctGuilds)
            .doOnNext(gce -> log.info("[Shard {}] Discovered guild \"{}\" ({}) with {} members.",
                gce.getShardInfo().getIndex(),
                gce.getGuild().getName(),
                gce.getGuild().getId().asString(),
                gce.getGuild().getMemberCount()
            ))
            .map(gce -> gce.getGuild().getId())
            .zipWith(botConfigSource)
            .filter(predicate((id, cfg) -> !cfg.hasGuildConfig(id)))    // make sure guildConfig doesn't exist before setting it up
            .flatMap(function(GuildConfig::load))                       // load guild config
            .zipWith(botConfigSource)
            .map(function((guildCfg, botCfg) -> {
                botCfg.putGuildConfig(guildCfg);
                botCfg.getModuleHandler().setupModulesFor(guildCfg);
                return guildCfg;
            }))
            .flatMap(GuildConfig::update)
            .last();
    }
}
