package lyrth.makanism.api.util.buttons;

import discord4j.core.object.reaction.ReactionEmoji;
import emoji4j.Emoji;
import emoji4j.EmojiUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ReactionSet {
    Logger log = LoggerFactory.getLogger(ReactionSet.class);

    Pattern customEmojiPattern = Pattern.compile("<(a?):([A-Za-z0-9_]+):(\\d+)>");


    CustomReactionSet append(String... buttons);

    static CustomReactionSet custom(String... buttons){
        return new CustomReactionSet(buttons);
    }

    static CustomReactionSet custom(ReactionEmoji... buttons){
        return new CustomReactionSet(buttons);
    }

    Set<ReactionEmoji> getReactions();


    /// util

    @Nullable
    static ReactionEmoji getReactionEmoji(String s){
        Matcher matcher = customEmojiPattern.matcher(s);
        if (matcher.find()){
            try {
                long id = Long.parseLong(matcher.group(3));
                String name = matcher.group(2);
                boolean animated = "a".equals(matcher.group(1));
                return ReactionEmoji.of(id, name, animated);
            } catch (NumberFormatException | NullPointerException e){
                log.warn("Invalid emoji: [{}]", s);
                return null;
            }
        } else {
            String unicode = getUnicodeEmoji(s);
            if (unicode != null)
                return ReactionEmoji.unicode(unicode);
            else log.warn("Invalid emoji: [{}]", s);
            return null;
        }
    }

    static String getUnicodeEmoji(String name){
        return Optional.ofNullable(EmojiUtils.getEmoji(name)).map(Emoji::getEmoji).orElse(null);
    }
}
