package lyrth.makanism.api.react.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import lyrth.makanism.api.react.ReactionSet;
import lyrth.makanism.api.react.ReactionSets;
import reactor.core.publisher.Mono;

public class NoopReactListener extends ReactListener {

    public NoopReactListener() {
        super(Snowflake.of(0));
    }

    public NoopReactListener attach(Message message){
        this.message = message;
        return this;
    }

    @Override
    public Mono<?> on(ReactionAddEvent event) {
        return Mono.empty();
    }

    @Override
    public Mono<?> on(ReactionRemoveEvent event) {
        return Mono.empty();
    }

    @Override
    public ReactionSet getReactionSet() {
        return ReactionSets.NONE;
    }

    @Override
    public Mono<Message> start() {
        return Mono.just(message);
    }
}
