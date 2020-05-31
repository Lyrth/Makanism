package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.common.file.SourceProvider;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.impl.FileSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;

public class GatewayHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        final SourceProvider source = new FileSourceProvider("config");     // pick a SourceProvider impl

        return BotConfig.load(source, new HashMap<>(), new BotProps(), new ModuleHandler())
            .flatMap(botConfig -> {

                Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                    log.info("Shutdown initiated. Saving...");
                    botConfig.saveAll()             // TODO: disable modules
                        .and(client.logout().timeout(Duration.ofMinutes(2)).retry(3))
                        .block();
                    System.out.println("Shutting down.");
                }));

                return Mono.when(
                    ReadyHandler.handle(client),
                    GuildHandler.handle(client, botConfig),
                    botConfig.getModuleHandler().handle(client, botConfig),
                    ConsoleHandler.handle(client),
                    SaveHandler.handle(client, botConfig),
                    ReactionHandler.handle(client, botConfig),
                    updateAndLoadConfig(client, botConfig)
                        .flatMap(config -> CommandHandler.handle(client, config))
                        .onErrorContinue((t, $) -> log.error("CAUgHt eWWoW!", t))
                );
            });
    }

    private static Mono<BotConfig> updateAndLoadConfig(GatewayDiscordClient client, BotConfig botConfig){
        return client.getApplicationInfo()
            .doOnNext(appInfo -> botConfig.setIds(appInfo.getId(), appInfo.getOwnerId()))
            .then(botConfig.update())
            .cache()
            .doOnNext(config -> AccessLevel.setBotOwnerId(config.getOwnerId()))
            .doOnNext(config -> log.info("Loaded bot config."));
    }
}
