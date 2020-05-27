package lyrth.makanism.api.react;

import discord4j.core.object.reaction.ReactionEmoji;

import java.util.LinkedHashSet;
import java.util.Set;

public enum ReactionSets implements ReactionSet {

    // Mostly just for reference for now.
    DIR_OK("arrow_up","arrow_down","arrow_left","arrow_right","ok"),
    PAGE_NAV("arrow_backward","arrow_forward"),
    PAGE_NAV_ADV("previous_track_button","arrow_backward","arrow_forward","next_track_button","1234"),
    PLAYER("previous_track_button","play_or_pause_button","next_track_button","stop_button"),
    NUM_TEN("one","two","three","four","five","six","seven","eight","nine","keycap_ten"),
    YES_NO("white_check_mark","x"),
    NONE(),
    ;

    public static final ReactionSet DEFAULT = PAGE_NAV;

    private final LinkedHashSet<ReactionEmoji> reactions = new LinkedHashSet<>();

    ReactionSets(String... buttons){
        for (String b : buttons) {
            ReactionEmoji emoji = ReactionSet.getReactionEmoji(b);
            if (emoji != null) reactions.add(emoji);
        }
    }

    public CustomReactionSet append(String... buttons){
        return new CustomReactionSet(this).append(buttons);
    }

    public Set<ReactionEmoji> getReactions(){
        return reactions;
    }
}
