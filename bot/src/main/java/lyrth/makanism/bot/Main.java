package lyrth.makanism.bot;

import discord4j.common.GitProperties;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.gateway.intent.Intent;
import discord4j.gateway.intent.IntentSet;
import discord4j.store.api.service.StoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisException;
import lyrth.makanism.bot.handlers.GatewayHandler;
import lyrth.makanism.bot.util.BotProps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        log.debug("Installing BlockHound...");
//        BlockHound.install();
//        log.debug("BlockHound ready.");

        // load bot meta
        log.debug("Reading .properties...");
        BotProps tempProps = new BotProps();
        String tokenVar = tempProps.get("bot.token.var");

        // get the token from env var
        log.debug("Reading env var...");
        String token = System.getenv(tokenVar);

        if (token == null || token.isEmpty()){
            log.warn("Bot token is {}. Please update the {} environment variable with the bot's token. Quitting.",
                token == null ? "null" : "empty",
                tokenVar);
            return;
        }

        new Main(token, tempProps);
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

        DiscordClientBuilder.create(token).build().gateway()
            .setStoreService(getStoreService())
            .setEventDispatcher(EventDispatcher.replayingWithSize(16))
            .setDisabledIntents(IntentSet.of(
                Intent.GUILD_PRESENCES,
                Intent.GUILD_MEMBERS,
                Intent.GUILD_MESSAGE_TYPING,
                Intent.DIRECT_MESSAGE_TYPING
            ))
            .setInitialStatus(shardInfo -> Presence.doNotDisturb(Activity.listening("initialization sounds.")))
            .withGateway(GatewayHandler::create)        // TODO: Use withEventDispatcher for some events.
            .block();

        log.info("ded!");
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
