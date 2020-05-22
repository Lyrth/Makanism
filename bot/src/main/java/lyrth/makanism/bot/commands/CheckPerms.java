package lyrth.makanism.bot.commands;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.PermissionSet;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.ADMINISTRATOR)
public class CheckPerms extends GuildCommand {

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return Mono.justOrEmpty(ctx.getMember())
            .flatMap(Member::getBasePermissions)
            .flatMapIterable(PermissionSet::asEnumSet)
            .map(perm -> perm.name() + " " + perm.getValue() + "\n")
            .collect(StringBuilder::new, StringBuilder::append)
            .map(StringBuilder::toString)
            .flatMap(ctx::sendReply);
    }
}
