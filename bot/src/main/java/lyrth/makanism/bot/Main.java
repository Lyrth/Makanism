package lyrth.makanism.bot;

import com.google.gson.GsonBuilder;
import discord4j.common.GitProperties;
import discord4j.core.DiscordClient;
import discord4j.core.DiscordClientBuilder;
import discord4j.core.event.EventDispatcher;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Presence;
import discord4j.rest.util.Snowflake;
import discord4j.store.api.service.StoreService;
import discord4j.store.jdk.JdkStoreService;
import discord4j.store.redis.RedisStoreService;
import io.lettuce.core.RedisException;
import lyrth.makanism.bot.handlers.GatewayHandler;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.bot.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ServiceLoader;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
//        log.debug("Installing BlockHound...");
//        BlockHound.install();
//        log.debug("BlockHound ready.");

        String fileName = "scratch_test.json";
        try {
            new GsonBuilder()/*.setPrettyPrinting()*/.create().toJson(Snowflake.of(928579379673976L), new FileWriter(fileName));
        } catch (IOException ignored){

        }

        System.exit(0);

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

        DiscordClientBuilder.create(token).setDebugMode(false).build()
            .gateway()
            .setStoreService(getStoreService())
            .setInitialStatus(shardInfo -> Presence.doNotDisturb(Activity.listening("initialization sounds.")))
            .setEventDispatcher(EventDispatcher.buffering())
            .withGateway(GatewayHandler::create)
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
