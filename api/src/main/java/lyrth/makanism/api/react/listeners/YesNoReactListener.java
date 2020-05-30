package lyrth.makanism.api.react.listeners;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import emoji4j.EmojiUtils;
import lyrth.makanism.api.react.ReactionSet;
import lyrth.makanism.api.react.ReactionSets;
import reactor.core.publisher.Mono;

public class YesNoReactListener extends ReactListener {     // TODO: lol actually add some functionality.

    public YesNoReactListener(Snowflake invoker) {
        super(invoker);
    }

    @Override
    public Mono<?> on(ReactionAddEvent event) {
        return Mono.justOrEmpty(event.getEmoji().asUnicodeEmoji())
            .flatMap(emoji -> {
                if (emoji.getRaw().equals(EmojiUtils.getEmoji("white_check_mark").getEmoji()))
                    return event.getChannel().flatMap(ch -> ch.createMessage("check! :o"));
                if (emoji.getRaw().equals(EmojiUtils.getEmoji("x").getEmoji()))
                    return cancel();
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
