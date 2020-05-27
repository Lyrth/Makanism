package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import lyrth.makanism.common.file.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

public class SaveHandler {
    private static final Logger log = LoggerFactory.getLogger("SaveHandler");

    private static final Duration SAVE_INTERVAL = Duration.ofMinutes(3);

    public static Mono<?> handle(GatewayDiscordClient client, BotConfig botConfig){
        return Flux.interval(SAVE_INTERVAL)     // interval already publishes on the parallel scheduler
            .doOnNext(n -> log.trace("Saving..."))
            .flatMap(n -> botConfig.saveAll().thenReturn(n))
            .doOnNext(n -> log.trace("Save {} complete.", n))
            .filter(n -> n % 40 == 0)       // every 2 hours
            .doOnNext(n -> log.debug("Save {} complete.", n))
            .then();
    }
}
