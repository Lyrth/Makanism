package lyrth.makanism.api.util.buttons;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class NoopReactListener extends ReactListener {

    public NoopReactListener(){ }

    public NoopReactListener(Message message){
        this.message = message;
    }

    public NoopReactListener attach(Message message){
        this.message = message;
        return this;
    }

    @Override
    public Mono<Void> on(ReactionAddEvent event) {
        return Mono.empty();
    }

    @Override
    public Mono<Void> on(ReactionRemoveEvent event) {
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
