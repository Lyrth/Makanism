package lyrth.makanism.api.util.buttons;

import com.vdurmont.emoji.Emoji;
import com.vdurmont.emoji.EmojiManager;
import discord4j.core.object.reaction.ReactionEmoji;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.annotation.Nullable;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface ReactionSet {
    Logger log = LoggerFactory.getLogger(ReactionSet.class);

    CustomReactionSet append(String... buttons);

    static CustomReactionSet custom(String... buttons){
        return new CustomReactionSet(buttons);
    }

    static CustomReactionSet custom(ReactionEmoji... buttons){
        return new CustomReactionSet(buttons);
    }

    Set<ReactionEmoji> getReactions();


    /// util

    Pattern customEmojiPattern = Pattern.compile("<(a?):([A-Za-z0-9_]+):(\\d+)>");
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
            return ReactionEmoji.unicode(getUnicodeEmoji(s));
        }
    }

    static String getUnicodeEmoji(String name){
        return Optional.ofNullable(EmojiManager.getForAlias(name)).map(Emoji::getUnicode).orElse(name);
    }
}
