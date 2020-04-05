package lyrth.makanism.bot;

import discord4j.common.GitProperties;
import discord4j.core.DiscordClient;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.store.api.service.StoreService;
import discord4j.store.jdk.JdkStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisException;
import lyrth.makanism.bot.handlers.ConnectionHandler;
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
        String tokenVar = BotProps.get("bot.token.var");

        // get the token from env var
        log.debug("Reading env var...");
        String token = System.getenv(tokenVar);

        if (token == null || token.isEmpty()){
            log.warn("Bot token is {}. Please update the {} environment variable with the bot's token. Quitting.",
                token == null ? "null" : "empty",
                tokenVar);
            return;
        }

        new Main(token);
    }

    private Main(String token){
        log.info("{} version {} starting.",
            BotProps.get("bot.name"),
            BotProps.get("bot.version")
        );
        log.info("Running on Discord4J version {} (Git {}:{} built on {}).",
            BotProps.get("d4j.version"),
            BotProps.D4JProps.get(GitProperties.APPLICATION_VERSION),
            BotProps.D4JProps.get("git.commit.id.abbrev"),
            BotProps.D4JProps.get("git.commit.time")
        );

        DiscordClient.create(token).gateway()
            .setStoreService(getStoreService())
            .setInitialStatus(shardInfo -> Presence.doNotDisturb(Activity.listening("initialization sounds.")))
            .setEventDispatcher(EventDispatcher.buffering())
            .withConnection(ConnectionHandler::create)
            .block();

        log.info("ded!");
    }

    private StoreService getStoreService(){
        try {
            return new RedisStoreService();
        } catch (RedisException e) {
            log.warn("Redis store service error! Using JdkStoreService instead. ({})", e.getMessage());
            return new JdkStoreService();
        }
    }

}
