package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.common.util.file.SourceProvider;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.impl.FileSourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public class GatewayHandler {
    private static final Logger log = LoggerFactory.getLogger(GatewayHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        SourceProvider source = new FileSourceProvider("config");
        BotProps props = new BotProps();

        BotConfig botConfig = BotConfig.lazyLoad(source, props);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown initiated. Saving...");
            botConfig.saveAll().and(client.logout()).block();
            log.info("Shutting down.");
        }));

        // todo: config save timer
        return Mono.when(
                ReadyHandler.handle(client),
                GuildHandler.handle(client, botConfig, source, props),
                ConsoleHandler.handle(client),
                Mono.zip(client.getApplicationInfo(), botConfig.load(),
                    (appInfo, config) -> config.setIds(appInfo.getId(), appInfo.getOwnerId())
                )
                    .flatMap(BotConfig::update)
                    .cache()
                    .doOnNext(config -> log.info("Loaded bot config."))     // todo ModuleHandler (first)
                    .flatMap(config -> CommandHandler.handle(client, config, props))
                    .onErrorContinue((t,$) -> log.error("CAUght eWWoW!", t))
            );
    }

}
