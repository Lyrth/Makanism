package lyrth.makanism.api.util.buttons;

import discord4j.core.object.reaction.ReactionEmoji;

import java.util.LinkedHashSet;
import java.util.Set;

public enum ReactionSets implements ReactionSet {

    DIR_OK("arrow_up","arrow_down","arrow_left","arrow_right","ok"),
    PAGE_NAV("rewind","fast_forward"),
    PAGE_NAV_ADV("black_left_pointing_double_triangle_with_vertical_bar","rewind","fast_forward","black_right_pointing_double_triangle_with_vertical_bar","1234"),
    // v "track_previous","play_pause","track_next","stop_button"
    PLAYER("black_left_pointing_double_triangle_with_vertical_bar","black_right_pointing_triangle_with_double_vertical_bar","black_right_pointing_double_triangle_with_vertical_bar","black_square_for_stop"),
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
