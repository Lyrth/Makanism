package lyrth.makanism.bot.commands.general;

import discord4j.common.util.Snowflake;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.bot.handlers.CommandHandler;
import lyrth.makanism.bot.handlers.ModuleHandler;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

@CommandInfo(
    name = "Commands",
    aliases = {"cmds"},
    accessLevel = AccessLevel.OWNER,
    desc = "List commands for a specific category or module, or lists all categories.",
    usage = "[<category>]"
)
public class CommandsCmd extends BotCommand {

    protected static final String CAT_LIST_MSG = "Command categories: ```%s``` Get the commands for each category by running `%scommands <category>`";
    protected static final String CMD_LIST_MSG = "Commands for %s: ```%s``` Find info for each command through `%shelp <command name>`";

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().isEmpty()){       // return list of categories
            return sendCategories(ctx, getAllCategories());
        }

        String category = ctx.getArg(1);

        Set<String> enabledModules = ctx.getBotConfig()
            .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
            .getEnabledModules();

        String commandList = Stream.concat(
                CommandHandler.getCommands().values().stream(),                                         // take base commands
                getCommands((ModuleHandler) ctx.getBotConfig().getModuleHandler()).stream()
                    .filter(cmd -> enabledModules.contains(cmd.getParentModuleName().toLowerCase()))    // only take commands from enabled modules
            )
            .filter(cmd -> cmd.getCategory().equalsIgnoreCase(category))    // matching category
            .map(cmd -> cmd.getName().toLowerCase())
            .distinct()                                                     // Duplicates happen due to aliases
            .sorted()
            .map(ctx.getPrefix()::concat)                                   // Append the prefix to the beginning
            .map("\n"::concat)                                              // Add newline before each item
            .collect(StringBuilder::new, StringBuilder::append, (a,b)->{})  // Append all items into a StringBuilder
            .toString();

        if (commandList.isBlank())
            return ctx.sendReply(
                enabledModules.contains(category.toLowerCase()) ?
                    "Module doesn't have commands." :
                    "Invalid category!"
            );

        return ctx.sendReply(String.format(CMD_LIST_MSG, category, commandList, ctx.getPrefix()));
    }

    protected static Mono<Void> sendCategories(CommandCtx ctx, Set<String> baseCategories){
        Stream<String> modules = ctx.getBotConfig()                 // Gets the list of enabled modules for the guild
            .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
            .getEnabledModules()
            .stream()
            .map(s -> s.substring(0,1).toUpperCase() + s.substring(1))      // Capitalize first letter
            .sorted();                                                      // Sort alphabetically

        String categories = Stream.concat(modules, baseCategories.stream().sorted())
            .distinct()
            .map("\n"::concat)                                              // Add newline before each item
            .collect(StringBuilder::new, StringBuilder::append, (a,b)->{})  // Append all items into a StringBuilder
            .toString();

        if (categories.isBlank()) return ctx.sendReply("There are no categories... Weird.");

        return ctx.sendReply(String.format(CAT_LIST_MSG, categories, ctx.getPrefix()));
    }

    private static Collection<GuildModuleCommand<GuildModule<?>>> getCommands(ModuleHandler moduleHandler){
        return moduleHandler.getGuildModuleCmds().values();
    }
}
