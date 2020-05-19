package lyrth.makanism.api.util;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuples;

import java.time.Duration;

public abstract class ReactListener {


    private transient final FluxProcessor<ReactionEvent, ReactionEvent> reactionProcessor;

    private final Snowflake channelId;
    private final Snowflake messageId;

    // private final long endTime;   // how to implement- ;-;

    // should be always called in subclasses through super
    public ReactListener(Snowflake channelId, Snowflake messageId){
        this.channelId = channelId;
        this.messageId = messageId;

        MenuRegistry.register(this);

        reactionProcessor = MenuRegistry.getProcessorFor(this.messageId, this::cancelTask);

        //this.endTime = 0;
    }

    // default
    public Flux<ReactionEvent> cancelTask(Flux<ReactionEvent> source){
        return source.timeout(Duration.ofSeconds(15),
            source.map(e -> Tuples.of(e.getClient(), e.getMessageId()))
                .flatMap(TupleUtils.function(MenuRegistry::removeListener))
                .then(Mono.empty())
            );
    }

    public Mono<Void> start(){
        return reactionProcessor.flatMap(reactionEvent ->
            reactionEvent.isAddEvent() ?
                this.on(reactionEvent.getAddEvent()) :
                this.on(reactionEvent.getRemoveEvent()))
            .then();
    }

    public abstract Mono<Void> on(ReactionAddEvent event);
    public abstract Mono<Void> on(ReactionRemoveEvent event);

    public Snowflake getChannelId() {
        return channelId;
    }

    public Snowflake getMessageId() {
        return messageId;
    }
}