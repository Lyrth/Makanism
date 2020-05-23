package lyrth.makanism.api.util;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import lyrth.makanism.api.util.buttons.NoopReactListener;
import lyrth.makanism.api.util.buttons.ReactListener;
import reactor.core.publisher.Mono;

/// A reply that acts as a menu through the use of reactions.
/// Can also attach to existing messages to observe reactions.
/// TODO: Persistence between restarts, 10 passive (non-command related) listeners per guild
public class MenuMessage extends Reply<MenuMessage> {

    protected ReactListener listener = new NoopReactListener();

    public static MenuMessage create(String content){
        return new MenuMessage().setContent(content);
    }

    public static MenuMessage create(String content, ReactListener listener){
        return withListener(listener).setContent(content);
    }

    public static MenuMessage withListener(ReactListener listener){
        return new MenuMessage().setListener(listener);
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
