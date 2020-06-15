package lyrth.makanism.bot.commands.admin;

import discord4j.common.util.Snowflake;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandInfo(
    name = "Module",
    aliases = {"mods", "modules"},
    accessLevel = AccessLevel.OWNER,
    desc = "Enables or disables a guild module, or lists the enabled ones.",
    usage = "[enable|disable <module name>]"
)
public class ModuleCmd extends GuildCommand {

    private static final String MODULE_LIST_MSG = "List of enabled modules: ```%s```";
    private static final String SUCCESS_MESSAGE = "%s module %sd.";
    private static final String ALREADY_MESSAGE = "%s module is already %sd.";

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().isEmpty()){       // return list of enabled modules
            String items = ctx.getBotConfig()                       // Gets the list of enabled modules for the guild
                .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
                .getEnabledModules()
                .stream()
                .map(s -> s.substring(0,1).toUpperCase() + s.substring(1))          // Capitalize first letter
                .sorted()                                                           // Sort alphabetically
                .map("\n"::concat)                                                  // Add newline before each item
                .collect(StringBuilder::new, StringBuilder::append, (a,b) -> {})    // Append all items into a StringBuilder
                .toString();

            if (items.isBlank()) return ctx.sendReply("There are no enabled modules.");

            return ctx.sendReply(String.format(MODULE_LIST_MSG, items));
        }

        String mode = ctx.getArg(1).toLowerCase();
        String moduleName = ctx.getArg(2).toLowerCase();

        // if first arg isn't enable/disable, or the arg is just enable/disable
        if (!mode.matches("enable|disable") || ctx.getArgs().count() == 1)
            return ctx.sendReply("Invalid args.");

        Optional<Boolean> success;
        if (mode.equals("enable")){
            success = ctx.getBotConfig().enableGuildModule(moduleName, ctx.getGuildId().orElse(Snowflake.of(0L)));
        } else {
            success = ctx.getBotConfig().disableGuildModule(moduleName, ctx.getGuildId().orElse(Snowflake.of(0L)));
        }

        if (success.isEmpty()) return ctx.sendReply("Module not found.");

        if (success.get())
            return ctx.sendReply(String.format(SUCCESS_MESSAGE, moduleName, mode));     // success
        else
            return ctx.sendReply(String.format(ALREADY_MESSAGE, moduleName, mode));     // already enabled
    }
}
