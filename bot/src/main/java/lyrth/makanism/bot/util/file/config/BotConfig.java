package lyrth.makanism.bot.util.file.config;

import discord4j.rest.util.Snowflake;
import lyrth.makanism.bot.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.HashSet;

public class BotConfig {
    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    private String defaultPrefix = ";;";
    private Snowflake botId;
    private Snowflake ownerId;

    private HashSet<String> disabledCommands;   // global blacklist

    private SourceProvider source;

    public static Mono<BotConfig> load(SourceProvider source){
        String path = "bot/bot";
        return source.read(path, BotConfig.class);
    }

    // Write to resource
    public Mono<Void> update(){
        String path = "bot/bot";
        return source.write(path, this);
    }

}
