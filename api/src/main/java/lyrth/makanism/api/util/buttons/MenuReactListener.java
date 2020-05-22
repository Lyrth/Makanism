package lyrth.makanism.api.util.buttons;

import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.reaction.ReactionEmoji;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.LinkedHashMap;
import java.util.function.Function;

public class MenuReactListener extends ReactListener {  // TODO setDuration

    private final LinkedHashMap<ReactionEmoji, Action> actions = new LinkedHashMap<>();

    /// Note that addAction methods will replace previously added entries.
    // default toggle = off: reaction auto-removed.
    public MenuReactListener addAction(String emoji, Function<ReactionAddEvent, Mono<Void>> onAdd){
        return addAction(emoji, onAdd, false);
    }

    // toggle off: bot removes reaction on add; toggle on: it doesn't remove the reaction after.
    public MenuReactListener addAction(String emoji, Function<ReactionAddEvent, Mono<Void>> onAdd, boolean isToggle){
        ReactionEmoji reactionEmoji = ReactionSet.getReactionEmoji(emoji);
        if (reactionEmoji != null) actions.put(reactionEmoji, new Action(onAdd, null, isToggle));
        return this;
    }

    // automatic toggle = on
    public MenuReactListener addAction(String emoji, @Nullable Function<ReactionAddEvent, Mono<Void>> onAdd, @Nullable Function<ReactionRemoveEvent, Mono<Void>> onRemove){
        ReactionEmoji reactionEmoji = ReactionSet.getReactionEmoji(emoji);
        if (reactionEmoji != null) actions.put(reactionEmoji, new Action(onAdd, onRemove, true));
        return this;
    }

    public MenuReactListener cancelOn(String emoji){
        ReactionEmoji reactionEmoji = ReactionSet.getReactionEmoji(emoji);
        if (reactionEmoji != null) actions.put(reactionEmoji, new Action(e -> cancel(),null,true));
        return this;
    }

    @Override
    public Mono<Void> on(ReactionAddEvent event) {
        return Mono.justOrEmpty(actions.get(event.getEmoji()))
            .flatMap(action -> action.onAdd(event));
    }

    @Override
    public Mono<Void> on(ReactionRemoveEvent event) {
        return Mono.justOrEmpty(actions.get(event.getEmoji()))
            .flatMap(action -> action.onRemove(event));
    }

    @Override
    public ReactionSet getReactionSet() {
        return ReactionSet.custom(actions.keySet().toArray(new ReactionEmoji[0]));
    }
}

class Action {
    private final Function<ReactionAddEvent, Mono<Void>> onAdd;
    @Nullable private final Function<ReactionRemoveEvent, Mono<Void>> onRemove;

    protected Action(@Nullable Function<ReactionAddEvent, Mono<Void>> onAdd, @Nullable Function<ReactionRemoveEvent, Mono<Void>> onRemove, boolean isToggle){
        this.onRemove = onRemove;

        // only respect toggling when onRemove doesn't exist, because toggle is *needed* for onRemove.
        boolean toggleable = (onRemove != null) || isToggle;

        if (!toggleable){        // if the action isn't a toggle, automatically remove the reaction on add
            this.onAdd = e -> e.getMessage()
                .flatMap(m -> m.removeReaction(e.getEmoji(), e.getUserId()))
                .then(Mono.justOrEmpty(onAdd).flatMap(f -> f.apply(e)));
        } else {
            this.onAdd = e -> Mono.justOrEmpty(onAdd).flatMap(f -> f.apply(e));
        }
    }

    public Mono<Void> onAdd(ReactionAddEvent e){
        return this.onAdd.apply(e);
    }

    public Mono<Void> onRemove(ReactionRemoveEvent e){
        return Mono.justOrEmpty(this.onRemove).flatMap(f -> f.apply(e));
    }
}