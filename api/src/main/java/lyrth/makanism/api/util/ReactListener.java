package lyrth.makanism.api.util;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

public abstract class ReactListener {

    // stores messageId - listener pairs.
    private transient static final ConcurrentHashMap<Snowflake, ? extends ReactListener> instances = new ConcurrentHashMap<>();

    private long endTime;   // how to implement- ;-;

    // messageId
    public ReactListener(){
        MenuRegistry.register(this.getClass().getSimpleName(), this);
    }

    // dispatch to a *single* instance of on, that matches the message id.
    final Mono<Void> dispatch(ReactionAddEvent event){
        return Mono.justOrEmpty(instances.get(event.getMessageId()))
            .flatMap(listener -> listener.on(event))
            .then();
    }

    final Mono<Void> dispatch(ReactionRemoveEvent event){
        return Mono.justOrEmpty(instances.get(event.getMessageId()))
            .flatMap(listener -> listener.on(event))
            .then();
    }

    public abstract Mono<Void> on(ReactionAddEvent event);
    public abstract Mono<Void> on(ReactionRemoveEvent event);

}
