package lyrth.makanism.bot.commands.admin;

import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

import java.util.Map;

@CommandInfo(
    aliases = {"setAlias"},
    accessLevel = AccessLevel.OWNER,
    desc =
        "Creates an alias that, when run, executes another arbitrary command with or without additional arguments.\n" +
            "Not specifying the target removes the alias. Run this without args to see the server's command aliases.\n" +
            "For example, doing:\n" +
            "`${prefix}alias something help userInfo`\n" +
            "Sending `${prefix}something` would now be equivalent to doing `${prefix}help userinfo`.",
    usage = "[<alias - no spaces>] [<target>]"
)
public class Alias extends GuildCommand {

    private static final String AL_LIST_MSG = "Active guild aliases: %s \n*(Note: ~bt~ is backtick: ` )*";
    private static final String CLEARED_MSG = "Alias `%s` cleared.";
    private static final String INVALID_MSG = "Alias `%s` invalid or doesn't exist.";
    private static final String SUCCESS_MSG = "Alias `%s` now points to `%s`.";

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().count() < 1){
            StringBuilder items = new StringBuilder("\n");
            for (Map.Entry<String, String> entry : ctx.getGuildConfig().getAliases().entrySet()){
                items.append('`').append(entry.getKey().replace("`","~bt~")).append('`');
                items.append(" -> ");
                items.append('`').append(entry.getValue().replace("`","~bt~")).append('`');
                items.append('\n');
            }
            return ctx.sendReply(String.format(AL_LIST_MSG, items.toString()));
        }

        String alias = ctx.getArg(1).toLowerCase();
        String command = ctx.getArgs().getRest(2);
        if (command.isBlank()) {        // remove
            String removed = ctx.getGuildConfig().getAliases().remove(alias);
            if (removed != null)
                return ctx.sendReply(String.format(CLEARED_MSG, alias));
            else
                return ctx.sendReply(String.format(INVALID_MSG, alias));
        } else {                        // add
            String target = (command + ctx.getArgs().concatFlags()).trim();
            ctx.getGuildConfig().getAliases().put(alias, target);
            return ctx.sendReply(String.format(SUCCESS_MSG, alias, target));
        }
    }
}
