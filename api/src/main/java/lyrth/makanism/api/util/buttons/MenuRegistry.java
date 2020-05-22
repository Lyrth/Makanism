package lyrth.makanism.api.util.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// todo: Map names to listeners to allow persistence.
public class MenuRegistry {
    private static final Logger log = LoggerFactory.getLogger(MenuRegistry.class);

    // stores messageId - listener pairs.
    private transient static final ConcurrentHashMap<Snowflake, ReactListener> listeners = new ConcurrentHashMap<>();
    private transient static final FluxProcessor<ReactionEvent, ReactionEvent> processor = DirectProcessor.create();
    private transient static final FluxSink<ReactionEvent> sink = processor.sink(FluxSink.OverflowStrategy.DROP);

    static void register(ReactListener listener){
        listeners.put(listener.getMessageId(), listener);
    }

    static FluxProcessor<ReactionEvent, ReactionEvent> getProcessorFor(
        Snowflake messageId,
        Function<Flux<ReactionEvent>, Flux<ReactionEvent>> cancelHook
    ){
        return FluxProcessor.wrap(EmitterProcessor.create(),
            processor.filter(e -> e.getMessageId().equals(messageId))
                .transform(cancelHook));
    }

    static Mono<Void> removeListener(GatewayDiscordClient client, Snowflake messageId){
        return Mono.just(messageId)
            .filter(listeners::containsKey)
            .map(listeners::remove)
            .map(ReactListener::getChannelId)
            .flatMap(channelId -> client.getMessageById(channelId, messageId))
            .flatMap(Message::removeAllReactions);
    }

    // dispatch to every *type* of listener
    public static void listen(ReactionAddEvent event){
        sink.next(new ReactionEvent(event));
    }

    public static void listen(ReactionRemoveEvent event){
        sink.next(new ReactionEvent(event));
    }


    // GuildConfig: active listeners: {messageId: listener}
}
