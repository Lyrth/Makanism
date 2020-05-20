package lyrth.makanism.api.util;

import discord4j.core.object.reaction.ReactionEmoji;

import java.util.Collection;
import java.util.LinkedHashMap;

public class CustomReactionSet implements ReactionSet {

    private final LinkedHashMap<String, ReactionEmoji> reactions = new LinkedHashMap<>();

    public CustomReactionSet(String... buttons){
        for (String b : buttons) {
            ReactionEmoji emoji = ReactionSet.getReactionEmoji(b);
            if (emoji != null) reactions.put(b, emoji);
        }
    }

    public CustomReactionSet(ReactionSet from){
        reactions.putAll(from.getReactionMap());
    }

    public CustomReactionSet append(String... buttons){
        for (String b : buttons) {
            ReactionEmoji emoji = ReactionSet.getReactionEmoji(b);
            if (emoji != null) reactions.put(b, emoji);
        }
        return this;
    }

    public Collection<ReactionEmoji> getReactions(){
        return reactions.values();
    }

    public LinkedHashMap<String, ReactionEmoji> getReactionMap(){
        return reactions;
    }
}
