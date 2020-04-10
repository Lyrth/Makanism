package lyrth.makanism.bot.commands;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.PermissionSet;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.CommandEvent;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.ADMINISTRATOR)
public class CheckPerms extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(CheckPerms.class);

    @Override
    public Mono<Void> execute(CommandEvent e) {
        return Mono.justOrEmpty(e.getMember())
            .flatMap(Member::getBasePermissions)
            .flatMapIterable(PermissionSet::asEnumSet)
            .map(perm -> perm.name() + " " + perm.getValue() + "\n")
            .collect(StringBuilder::new, StringBuilder::append)
            .map(StringBuilder::toString)
            .zipWith(e.getChannel(), (msg, ch) -> ch.createMessage(msg))
            .flatMap(msg -> msg)
            .then();
    }
}
