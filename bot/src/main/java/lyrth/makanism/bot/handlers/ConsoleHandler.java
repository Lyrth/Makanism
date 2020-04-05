package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ConsoleHandler extends Handler {
    private static final Logger log = LoggerFactory.getLogger(ConsoleHandler.class);

    public static Mono<Void> create(GatewayDiscordClient client){
        return new ConsoleHandler(client).handlers();
    }

    private ConsoleHandler(GatewayDiscordClient client){
        handlers = Flux.fromStream(new BufferedReader(new InputStreamReader(System.in)).lines())    // console lines
            .flatMap(this::handleConsoleCommand)
            .subscribeOn(Schedulers.single())    // subscribe on a different thread to not block the main thread
            .then();
    }

    private Mono<Void> handleConsoleCommand(String line){
        return Mono.just(line)
            .doOnNext(log::info)
            .filter(s -> s.matches("^(shutdown|stop|exit|quit) ?"))  // smol to do: possible arg
            .doOnNext($ -> System.exit(0))
            .then();
    }

}
