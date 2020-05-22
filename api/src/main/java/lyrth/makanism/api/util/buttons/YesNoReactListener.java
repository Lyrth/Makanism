package lyrth.makanism.api.util.buttons;

import com.vdurmont.emoji.EmojiManager;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import reactor.core.publisher.Mono;

public class YesNoReactListener extends ReactListener {     // TODO: lol actually add some functionality.

    @Override
    public Mono<?> on(ReactionAddEvent event) {
        return Mono.justOrEmpty(event.getEmoji().asUnicodeEmoji())
            .flatMap(emoji -> {
                if (emoji.getRaw().equals(EmojiManager.getForAlias("white_check_mark").getUnicode()))
                    return event.getChannel().flatMap(ch -> ch.createMessage("check! :o"));
                if (emoji.getRaw().equals(EmojiManager.getForAlias("x").getUnicode()))
                    return cancel();     // FIXME: don't delete, cancel instead.
                return Mono.empty();
            });
    }

    @Override
    public Mono<?> on(ReactionRemoveEvent event) {
        return Mono.empty();
    }

    @Override
    public ReactionSet getReactionSet() {
        return ReactionSets.YES_NO;
    }
}
