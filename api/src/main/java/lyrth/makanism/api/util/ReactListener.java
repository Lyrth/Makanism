package lyrth.makanism.api.util;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;
import reactor.util.function.Tuples;

import java.time.Duration;

public abstract class ReactListener {
    protected static final Logger log = LoggerFactory.getLogger(ReactListener.class);

    private transient FluxProcessor<ReactionEvent, ReactionEvent> reactionProcessor;

    protected Message message;

    // private final long endTime;   // how to implement- ;-;

    public ReactListener(){}

    // should be always called in subclasses through super
    public ReactListener(Message message){
        this.message = message;
        MenuRegistry.register(this);
        this.reactionProcessor = MenuRegistry.getProcessorFor(this.message.getId(), this::cancelTask);

        //this.endTime = 0;
    }

    public ReactListener attach(Message message){
        this.message = message;
        MenuRegistry.register(this);
        this.reactionProcessor = MenuRegistry.getProcessorFor(this.message.getId(), this::cancelTask);

        return this;
    }

    // default
    public Flux<ReactionEvent> cancelTask(Flux<ReactionEvent> source){
        return source.doOnNext(e -> log.info("Gets through.")).timeout(
            Mono.defer(() -> Mono.delay(Duration.ofSeconds(30))),             // Wait longer for the user's initial reaction.
            e -> Mono.delay(Duration.ofSeconds(15)),
            Mono.fromCallable(() -> Tuples.of(message.getClient(), message.getId()))
                .doOnNext(t -> log.info("TIMEOUT happened."))
                .flatMap(TupleUtils.function(MenuRegistry::removeListener))
                .then(Mono.fromRunnable(() -> reactionProcessor.onComplete()))
        );
    }

    public Mono<Message> start(){                      // TODO: check if message has same reaction set.
        return Mono.defer(() -> Mono.when(
            putReactions(),
            reactionProcessor
                .doOnNext(e -> log.debug("after processor"))
                .flatMap(reactionEvent ->
                    reactionEvent.isAddEvent() ?
                        this.on(reactionEvent.getAddEvent()) :
                        this.on(reactionEvent.getRemoveEvent()))
                .doOnEach(e -> log.debug("after on, type: " + e.getType().name()))
                .then()
            )
            .thenReturn(this.message));
    }

    private Mono<Void> putReactions(){              // TODO: partial addition when it has reactions already? delta
        return this.message.removeAllReactions()
            .thenMany(Flux.fromIterable(getReactionSet().getReactions()))
            .flatMap(this.message::addReaction)
            .doOnEach(s -> log.debug("done adding reactions."))
            .then();
    }

    public abstract ReactionSet getReactionSet();

    public abstract Mono<Void> on(ReactionAddEvent event);
    public abstract Mono<Void> on(ReactionRemoveEvent event);

    public Snowflake getChannelId() {
        return message.getChannelId();
    }

    public Snowflake getMessageId() {
        return message.getId();
    }
}