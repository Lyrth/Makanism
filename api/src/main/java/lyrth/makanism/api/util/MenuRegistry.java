package lyrth.makanism.api.util;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

// Maps names to listeners to allow persistence.
public class MenuRegistry {

    // stores the different types of listeners and their names
    // first instance created only taken.
    private static final ConcurrentHashMap<String, ReactListener> listeners = new ConcurrentHashMap<>();


    public static void register(String name, ReactListener listenerSet){
        listeners.putIfAbsent(name, listenerSet);
    }

    // dispatch to every *type* of listener
    public static Mono<Void> listen(ReactionAddEvent event){
        return Mono.just(listeners)
            .flatMapIterable(ConcurrentHashMap::values)
            .flatMap(listener -> listener.dispatch(event))
            .then();
    }

    public static Mono<Void> listen(ReactionRemoveEvent event){
        return Mono.just(listeners)
            .flatMapIterable(ConcurrentHashMap::values)
            .flatMap(listener -> listener.dispatch(event))
            .then();
    }


    // GuildConfig: active listeners: {name: listener}
}
