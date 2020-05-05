package lyrth.makanism.api.util;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

import java.time.Duration;

/// A reply that deletes itself after a set amount of time.
/// Not recommended for long runs, since the bot doesn't remember what should be deleted on restart.
public class EphemeralReply extends Reply {

    private Duration duration = Duration.ZERO;

    public static EphemeralReply withDuration(Duration duration){
        return new EphemeralReply().setDuration(duration);
    }

    public EphemeralReply setDuration(Duration duration){
        this.duration = duration;
        return this;
    }

    // This completes when message gets deleted instead. Returned message is a cached copy from creation.
    public Mono<Message> send(MessageChannel channel){
        return channel.createMessage(this::apply)
            .cache()
            .flatMap(msg ->
                duration.equals(Duration.ZERO) ?
                    Mono.just(msg) :
                    Mono.delay(duration).then(msg.delete()).thenReturn(msg)
            );
    }
}
