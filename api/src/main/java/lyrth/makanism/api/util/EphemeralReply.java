package lyrth.makanism.api.util;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.time.Duration;

/// A reply that deletes itself after a set amount of time.
/// Not recommended for long runs, since the bot won't remember what should be deleted on restart.
public class EphemeralReply extends Reply {

    private Duration duration = Duration.ZERO;

    /// Constructors

    public static EphemeralReply create(String content){
        return (EphemeralReply) new EphemeralReply().setContent(content);
    }

    public static EphemeralReply create(String content, Duration duration){
        return (EphemeralReply) withDuration(duration).setContent(content);
    }

    public static EphemeralReply withDuration(Duration duration){
        return new EphemeralReply().setDuration(duration);
    }


    /// Modifier methods

    public EphemeralReply setDuration(Duration duration){
        this.duration = duration;
        return this;
    }


    /// Terminal functions

    // This completes when message gets deleted instead. Returned message is a cached copy from creation.
    @Override
    public Mono<Message> send(MessageChannel channel){
        return sendAndLink(channel, null);
    }

    // When the ephemeral reply gets deleted, this would also delete linkedMessage.
    // If linkedMessage is null, it is ignored.
    // This completes when message gets deleted instead. Returned message is a cached copy from creation.
    public Mono<Message> sendAndLink(MessageChannel channel, @Nullable Message linkedMessage){
        return channel.createMessage(this::apply)
            .flatMap(msg ->
                duration.equals(Duration.ZERO) ?
                    Mono.just(msg) :
                    Mono.delay(duration)
                        .then(Mono.when(
                            msg.delete(),
                            Mono.justOrEmpty(linkedMessage).flatMap(Message::delete)
                        ))
                        .thenReturn(msg)
            );
    }

    // see sendAndLink above
    // Uses the command context message for linking, i.e., the invoking message
    public Mono<Message> sendAndLinkTo(CommandCtx ctx) {
        return ctx.getChannel().flatMap(ch -> sendAndLink(ch, ctx.getMessage()));
    }
}
