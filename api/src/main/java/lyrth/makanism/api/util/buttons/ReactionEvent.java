package lyrth.makanism.api.util.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class ReactionEvent {
    private final ReactionAddEvent addEvent;
    private final ReactionRemoveEvent removeEvent;

    protected ReactionEvent(ReactionAddEvent e){
        addEvent = e;
        removeEvent = null;
    }

    protected ReactionEvent(ReactionRemoveEvent e){
        removeEvent = e;
        addEvent = null;
    }

    protected boolean isAddEvent(){
        return addEvent != null;
    }

    protected boolean isRemoveEvent(){
        return removeEvent != null;
    }

    protected GatewayDiscordClient getClient(){
        if (addEvent != null) return addEvent.getClient();
        else assert removeEvent != null; return removeEvent.getClient();    // we are sure removeEvent isn't null anyways. assert just removes the warning
    }

    protected ReactionEmoji getEmoji(){
        if (addEvent != null) return addEvent.getEmoji();
        else assert removeEvent != null; return removeEvent.getEmoji();
    }

    protected Snowflake getChannelId(){
        if (addEvent != null) return addEvent.getChannelId();
        else assert removeEvent != null; return removeEvent.getChannelId();
    }

    protected Mono<Message> getMessage(){
        if (addEvent != null) return addEvent.getMessage();
        else assert removeEvent != null; return removeEvent.getMessage();
    }

    protected Snowflake getMessageId(){
        if (addEvent != null) return addEvent.getMessageId();
        else assert removeEvent != null; return removeEvent.getMessageId();
    }

    protected Snowflake getReactorId(){
        if (addEvent != null) return addEvent.getUserId();
        else assert removeEvent != null; return removeEvent.getUserId();
    }

    protected ReactionAddEvent getAddEvent(){
        return addEvent;
    }

    protected ReactionRemoveEvent getRemoveEvent(){
        return removeEvent;
    }

}
