package lyrth.makanism.api.react;

import discord4j.core.object.reaction.ReactionEmoji;

import java.util.LinkedHashSet;
import java.util.Set;

public class CustomReactionSet implements ReactionSet {

    private final LinkedHashSet<ReactionEmoji> reactions = new LinkedHashSet<>();

    public CustomReactionSet(String... buttons){
        for (String b : buttons) {
            ReactionEmoji emoji = ReactionSet.getReactionEmoji(b);
            if (emoji != null) reactions.add(emoji);
        }
    }
    public CustomReactionSet(ReactionEmoji... buttons){
        for (ReactionEmoji b : buttons) {
            if (b != null) reactions.add(b);
        }
    }

    public CustomReactionSet(ReactionSet from){
        reactions.addAll(from.getReactions());
    }

    public CustomReactionSet append(String... buttons){
        for (String b : buttons) {
            ReactionEmoji emoji = ReactionSet.getReactionEmoji(b);
            if (emoji != null) reactions.add(emoji);
        }
        return this;
    }

    public Set<ReactionEmoji> getReactions(){
        return reactions;
    }
}
