package lyrth.makanism.api.util.buttons;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.common.util.Snowflake;

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

    protected Snowflake getChannelId(){
        if (addEvent != null) return addEvent.getChannelId();
        else assert removeEvent != null; return removeEvent.getChannelId();
    }

    protected Snowflake getMessageId(){
        if (addEvent != null) return addEvent.getMessageId();
        else assert removeEvent != null; return removeEvent.getMessageId();
    }

    protected ReactionAddEvent getAddEvent(){
        return addEvent;
    }

    protected ReactionRemoveEvent getRemoveEvent(){
        return removeEvent;
    }

}
