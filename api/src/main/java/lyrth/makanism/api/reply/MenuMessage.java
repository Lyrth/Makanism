package lyrth.makanism.api.reply;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.api.react.listeners.NoopReactListener;
import lyrth.makanism.api.react.listeners.ReactListener;
import reactor.core.publisher.Mono;

/// A reply that acts as a menu through the use of reactions.
/// Can also attach to existing messages to observe reactions.
/// TODO: Persistence between restarts, 10 passive (non-command related) listeners per guild
public class MenuMessage extends Reply<MenuMessage> {

    protected ReactListener listener = new NoopReactListener();

    protected MenuMessage(Snowflake invokerUser) {
        super(invokerUser);
    }

    protected MenuMessage(CommandCtx invokerCtx) {
        super(invokerCtx);
    }

    public static MenuMessage create(Snowflake invokerUser, String content){
        return new MenuMessage(invokerUser).setContent(content);
    }

    public static MenuMessage create(CommandCtx invokerCtx, String content){
        return new MenuMessage(invokerCtx).setContent(content);
    }

    public static MenuMessage create(Snowflake invokerUser, String content, ReactListener listener){
        return new MenuMessage(invokerUser).setListener(listener).setContent(content);
    }

    public static MenuMessage create(CommandCtx invokerCtx, String content, ReactListener listener){
        return new MenuMessage(invokerCtx).setListener(listener).setContent(content);
    }


    /// Modifier methods

    public MenuMessage setListener(ReactListener listener){
        this.listener = listener;
        return this;
    }


    /// Terminal functions

    public Mono<Message> send(MessageChannel channel){
        return channel.createMessage(this::apply)
            .map(this.listener::attach)
            .flatMap(ReactListener::start);
    }
}
