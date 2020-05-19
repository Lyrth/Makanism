package lyrth.makanism.api.util;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

// todo: Map names to listeners to allow persistence.
public class MenuRegistry {

    // stores the different types of listeners and their names
    // first instance created only taken.
    //private static final ConcurrentHashMap<String, ReactListener> listeners = new ConcurrentHashMap<>();

    // stores messageId - listener pairs.
    private transient static final ConcurrentHashMap<Snowflake, ReactListener> listeners = new ConcurrentHashMap<>();
    private transient static final FluxProcessor<ReactionEvent, ReactionEvent> emitter = EmitterProcessor.create();
    private transient static final FluxSink<ReactionEvent> sink = emitter.sink(FluxSink.OverflowStrategy.BUFFER);

    //public static void register(String name, ReactListener listenerSet){
    //    listeners.putIfAbsent(name, listenerSet);
    //}

    static void register(ReactListener listener){
        listeners.put(listener.getMessageId(), listener);
    }

    static FluxProcessor<ReactionEvent, ReactionEvent> getProcessorFor(
        Snowflake messageId,
        Function<Flux<ReactionEvent>, Flux<ReactionEvent>> cancelHook
    ){
        return FluxProcessor.wrap(emitter,
            emitter.filter(e -> e.getMessageId().equals(messageId))
                .transform(cancelHook));
    }

    static Mono<Void> removeListener(GatewayDiscordClient client, Snowflake messageId){
        return Mono.just(messageId)
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
