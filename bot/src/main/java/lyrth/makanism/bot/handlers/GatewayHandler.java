package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.EventDispatcher;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.common.file.Props;
import lyrth.makanism.common.file.SourceProvider;
import lyrth.makanism.common.file.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;

public class GatewayHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayHandler.class);

    public static Mono<BotConfig> init(SourceProvider source, Props props){
        // Only load ModuleHandler once subscribed to, to not block main thread
        return Mono.defer(() -> BotConfig.load(source, new HashMap<>(), props, new ModuleHandler())).cache();
    }

    public static Mono<Void> onSetup(EventDispatcher dispatcher, Mono<BotConfig> config){
        return Mono.when(
            GuildHandler.handle(dispatcher, config),
            ReadyHandler.handle(dispatcher),
            ConsoleHandler.handle()
        );
    }

    public static Mono<Void> onConnect(GatewayDiscordClient client, BotConfig config){
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown initiated. Saving...");
            config.saveAll()             // TODO: disable modules
                .and(client.logout().timeout(Duration.ofMinutes(2)).retry(3))
                .block();
            System.out.println("Shutting down.");
        }));

        return Mono.when(
            config.getModuleHandler().handle(client, config),   // Initializing each guild module
            ReactionHandler.handle(client, config),
            SaveHandler.handle(client, config),
            updateAndLoadConfig(client, config)
                .then(CommandHandler.handle(client, config))
                .onErrorContinue((t, $) -> log.error("CAUgHt eWWoW!", t))
        );
    }

    private static Mono<BotConfig> updateAndLoadConfig(GatewayDiscordClient client, BotConfig botConfig){
        return client.getApplicationInfo()
            .map(appInfo -> botConfig.setIds(appInfo.getId(), appInfo.getOwnerId()))
            .flatMap(BotConfig::update)
            .cache()
            .doOnNext(config -> AccessLevel.setBotOwnerId(config.getOwnerId()))
            .doOnNext(config -> log.info("Loaded bot config."));
    }
}
