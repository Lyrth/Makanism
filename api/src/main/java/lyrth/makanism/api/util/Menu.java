package lyrth.makanism.api.util;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.function.Function;

public class Menu {

    private Snowflake messageId;

    private long beginTime;
    private long duration;

    private HashMap<ReactionEmoji, Function<ReactionAddEvent, Mono<Void>>> addActions;
    private HashMap<ReactionEmoji, Function<ReactionRemoveEvent, Mono<Void>>> removeActions;

    public Menu(Snowflake messageId){
        this.messageId = messageId;
    }


}
