package lyrth.makanism.bot;

import discord4j.common.GitProperties;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.core.shard.ShardingStrategy;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.store.api.service.StoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisException;
import lyrth.makanism.bot.handlers.GatewayHandler;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.common.file.SourceProvider;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.impl.FileSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import static reactor.function.TupleUtils.function;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        log.debug("Installing BlockHound...");
//        BlockHound.install();
//        log.debug("BlockHound ready.");

        // load bot meta
        log.debug("Reading .properties...");
        BotProps props = new BotProps();
        String tokenVar = props.get("bot.token.var");

        // get the token from env var
        log.debug("Reading env var...");
        String token = System.getenv(tokenVar);

        if (token == null || token.isBlank()){
            log.warn("Bot token is {}. Please update the {} environment variable with the bot's token. Quitting.",
                token == null ? "null" : "empty",
                tokenVar);
            return;
        }

        new Main(token, props);
    }

    private Main(String token, BotProps props){
        log.info("{} version {} starting.",
            props.get("bot.name"),
            props.get("bot.version"));

        log.info("On Java version {}",
            System.getProperty("java.runtime.version"));

        log.info("Running on Discord4J version {} (Git {}:{})",
            props.get("d4j.version"),
            BotProps.D4JProps.get(GitProperties.APPLICATION_VERSION),
            BotProps.D4JProps.get("git.commit.id.abbrev"));

        log.info("| built on {}.",
            BotProps.D4JProps.get("git.commit.time"));

        final SourceProvider source = new FileSourceProvider("config");     // pick a SourceProvider impl

        Mono<BotConfig> config = GatewayHandler.init(source, props);

        Mono<GatewayDiscordClient> client = DiscordClientBuilder.create(token).build().gateway()
            .setInitialStatus(shardInfo -> Presence.doNotDisturb(Activity.listening("initialization sounds.")))
            .setDisabledIntents(IntentSet.of(
                Intent.GUILD_PRESENCES,
                Intent.GUILD_MEMBERS,
                Intent.GUILD_MESSAGE_TYPING,
                Intent.DIRECT_MESSAGE_TYPING
            ))
            .setStoreService(getStoreService())
            .setSharding(ShardingStrategy.recommended())
            .withEventDispatcher(dispatcher -> GatewayHandler.onSetup(dispatcher, config))
            .setAwaitConnections(false)
            .login();

        Mono.zip(client, config)
            .flatMap(function(GatewayHandler::onConnect))
            .block();

    }

    private StoreService getStoreService(){
        try {
            return new RedisStoreService();
        } catch (RedisException e) {
            log.error("Redis store service error! Quitting.", e);
            System.exit(1);
            return null;
        }
    }
}
