package lyrth.makanism.bot.commands.admin;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

@CommandInfo(
    aliases = {"setAlias"},
    accessLevel = AccessLevel.OWNER,
    desc =
        "Creates an alias that, when run, executes another arbitrary command with or without additional arguments.\n" +
            "Not specifying the target removes the alias.\n" +
            "For example, doing:\n" +
            "`${prefix}alias something help userInfo`\n" +
            "Sending `${prefix}something` would now be equivalent to doing `${prefix}help userinfo`.",
    usage = "(<alias - no spaces>) [<target>]"
)
public class Alias extends GuildCommand {

    private static final String CLEARED_MSG = "Alias `%s` cleared.";
    private static final String SUCCESS_MSG = "Alias `%s` now points to `%s`.";

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().count() < 1){                 // TODO: list
            return ctx.sendReply("Invalid args.");
        }

        String alias = ctx.getArg(1).toLowerCase();
        String command = ctx.getArgs().getRest(2);
        if (command.isBlank()) {        // remove
            ctx.getGuildConfig().getAliases().remove(alias);
            return ctx.sendReply(String.format(CLEARED_MSG, alias));
        } else {                        // add
            ctx.getGuildConfig().getAliases().put(alias, command);
            return ctx.sendReply(String.format(SUCCESS_MSG, alias, command));
        }
    }
}
