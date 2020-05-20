package lyrth.makanism.api.util;

import com.vdurmont.emoji.EmojiManager;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.event.domain.message.ReactionRemoveEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public class YesNoReactListener extends ReactListener {

    public YesNoReactListener(){}

    public YesNoReactListener(Message message) {
        super(message);
    }

    @Override
    public Mono<Void> on(ReactionAddEvent event) {
        log.info("ReactionAddEvent arrived: " + event.getEmoji().toString());
        return Mono.justOrEmpty(event.getEmoji().asUnicodeEmoji())
            .flatMap(emoji -> {
                if (emoji.getRaw().equals(EmojiManager.getForAlias("white_check_mark").getUnicode()))
                    return event.getChannel().flatMap(ch -> ch.createMessage("check! :o")).then();
                if (emoji.getRaw().equals(EmojiManager.getForAlias("x").getUnicode()))
                    return event.getMessage().flatMap(Message::delete);     // FIXME: don't delete, cancel instead.
                return Mono.empty();
            });
    }

    @Override
    public Mono<Void> on(ReactionRemoveEvent event) {
        return Mono.empty();
    }

    @Override
    public ReactionSet getReactionSet() {
        return ReactionSets.YES_NO;
    }
}
