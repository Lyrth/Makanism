package lyrth.makanism.api.util.buttons;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxProcessor;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuples;

import java.time.Duration;

import static reactor.function.TupleUtils.function;

public abstract class ReactListener {
    // protected static final Logger log = LoggerFactory.getLogger(ReactListener.class);

    private transient FluxProcessor<ReactionEvent, ReactionEvent> reactionProcessor;

    protected Message message;
    protected final Snowflake invoker;      // User id of the initiator of the menu
    protected boolean invokerOnly = true;   // Whether to receive events from the invoker only or from everyone

    // private final long endTime;   // how to implement- ;-;

    public ReactListener(Snowflake invoker){
        this.invoker = invoker;
    }

    public ReactListener allowAllInteract(){    // allows everyone to interact with the reactions
        this.invokerOnly = false;
        return this;
    }

    public ReactListener attach(Message message){
        this.message = message;
        MenuRegistry.register(this);
        this.reactionProcessor = MenuRegistry.getProcessorFor(this.message.getId(), this::cancelTask);

        return this;
    }

    // the default task, can be overridden (can also super() call)
    public Flux<ReactionEvent> cancelTask(Flux<ReactionEvent> source){
        return source.timeout(
            Mono.defer(() -> Mono.delay(Duration.ofSeconds(30))),             // Wait longer for the user's initial reaction.
            e -> Mono.delay(Duration.ofSeconds(15)),
            cancel()
        );
    }

    // Cancels the listener and removes the reactions in the message.
    public final <T> Mono<T> cancel(){
        return Mono.fromCallable(() -> Tuples.of(message.getClient(), message.getId()))
            .flatMap(function(MenuRegistry::removeListener))
            .then(Mono.fromRunnable(() -> reactionProcessor.onComplete()));
    }

    public Mono<Message> start(){                   // TODO: check if message has same reaction set.
        return Mono.defer(() -> Mono.when(
            putReactions(),
            reactionProcessor
                .flatMap(this::removeIfNotAllowed)
                .flatMap(reactionEvent ->
                    reactionEvent.isAddEvent() ?
                        this.on(reactionEvent.getAddEvent()) :
                        this.on(reactionEvent.getRemoveEvent()))
                .then()
            )
            .thenReturn(this.message));
    }

    // Pass-through if reactor is allowed to react, becomes empty if isn't and removes the reaction.
    private Mono<ReactionEvent> removeIfNotAllowed(ReactionEvent reactionEvent){
        return Mono.just(reactionEvent)
            .filter(e -> !invokerOnly || e.getReactorId().equals(invoker))
            .switchIfEmpty(reactionEvent.getMessage()
                .flatMap(msg -> msg.removeReaction(reactionEvent.getEmoji(), reactionEvent.getReactorId()))
                .then(Mono.empty())
            );
    }

    // deferred until start()
    private Mono<Void> putReactions(){              // TODO: partial addition when it has reactions already? delta
        return this.message.removeAllReactions()
            .thenMany(Flux.fromIterable(getReactionSet().getReactions()))
            .flatMap(this.message::addReaction)
            .then();
    }

    // fetched on start()
    protected abstract ReactionSet getReactionSet();

    public abstract Mono<?> on(ReactionAddEvent event);
    public abstract Mono<?> on(ReactionRemoveEvent event);

    public Snowflake getChannelId() {
        return message.getChannelId();
    }

    public Snowflake getMessageId() {
        return message.getId();
    }
}