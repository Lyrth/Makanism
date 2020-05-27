package lyrth.makanism.api.react;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;

public class ReactionEvent {
    private final ReactionAddEvent addEvent;
    private final ReactionRemoveEvent removeEvent;

    public ReactionEvent(ReactionAddEvent e){
        addEvent = e;
        removeEvent = null;
    }

    public ReactionEvent(ReactionRemoveEvent e){
        removeEvent = e;
        addEvent = null;
    }

    public boolean isAddEvent(){
        return addEvent != null;
    }

    public boolean isRemoveEvent(){
        return removeEvent != null;
    }

    public GatewayDiscordClient getClient(){
        if (addEvent != null) return addEvent.getClient();
        else assert removeEvent != null; return removeEvent.getClient();    // we are sure removeEvent isn't null anyways. assert just removes the warning
    }

    public ReactionEmoji getEmoji(){
        if (addEvent != null) return addEvent.getEmoji();
        else assert removeEvent != null; return removeEvent.getEmoji();
    }

    public Snowflake getChannelId(){
        if (addEvent != null) return addEvent.getChannelId();
        else assert removeEvent != null; return removeEvent.getChannelId();
    }

    public Mono<Message> getMessage(){
        if (addEvent != null) return addEvent.getMessage();
        else assert removeEvent != null; return removeEvent.getMessage();
    }

    public Snowflake getMessageId(){
        if (addEvent != null) return addEvent.getMessageId();
        else assert removeEvent != null; return removeEvent.getMessageId();
    }

    public Snowflake getReactorId(){
        if (addEvent != null) return addEvent.getUserId();
        else assert removeEvent != null; return removeEvent.getUserId();
    }

    public ReactionAddEvent getAddEvent(){
        return addEvent;
    }

    public ReactionRemoveEvent getRemoveEvent(){
        return removeEvent;
    }

}
