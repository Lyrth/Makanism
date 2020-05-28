package lyrth.makanism.bot.commands.admin;

import discord4j.core.object.entity.Member;
import discord4j.rest.util.PermissionSet;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.ADMINISTRATOR,
    desc = "Gets the base permissions of the member that invoked this."
)
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
