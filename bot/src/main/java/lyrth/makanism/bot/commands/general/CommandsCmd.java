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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Set;

import static java.util.function.Predicate.not;

@CommandInfo(
    name = "Commands",
    aliases = {"cmds"},
    accessLevel = AccessLevel.OWNER,
    desc = "List commands for a specific category or module, or lists all categories.",
    usage = "[<category>]"
)
public class CommandsCmd extends BotCommand {       // FIXME: just do imperative lmao

    protected static final String CAT_LIST_MSG = "Command categories: ```%s``` Get the commands for each category by running `%scommands <category>`";
    protected static final String CMD_LIST_MSG = "Commands for %s: ```%s``` Find info for each command through `%shelp <command name>`";

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        if (ctx.getArgs().isEmpty()){       // return list of categories
            return getCategories(ctx, getAllCategories());
        } else {
            // TODO: cache. but where?

            Set<String> enabledModules = ctx.getBotConfig()
                .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
                .getEnabledModules();

            return Flux.fromIterable(CommandHandler.getCommands().values())
                .concatWith(
                    Flux.fromIterable(getCommands((ModuleHandler) ctx.getBotConfig().getModuleHandler()))
                        .filter(cmd -> enabledModules.contains(cmd.getParentModuleName().toLowerCase()))    // only take commands from enabled modules
                )
                .filter(cmd -> cmd.getCategory().equalsIgnoreCase(ctx.getArg(1)))
                .map(cmd -> cmd.getName().toLowerCase())
                .distinct()                                                     // Duplicates happen due to aliases
                .sort()
                .map(ctx.getPrefix()::concat)                                   // Append the prefix to the beginning
                .map("\n"::concat)                                              // Add newline before each item
                .collect(StringBuilder::new, StringBuilder::append)             // Append all items into a StringBuilder
                .map(StringBuilder::toString)                                   // Convert to String
                .filter(not(String::isBlank))                                   // Ensure it isn't blank
                .map(s -> String.format(CMD_LIST_MSG, ctx.getArg(1), s, ctx.getPrefix()))
                .defaultIfEmpty("Invalid category!")
                .flatMap(ctx::sendReply);
        }
    }

    protected static Mono<Void> getCategories(CommandCtx ctx, Set<String> baseCategories){
        return Flux.fromIterable(ctx.getBotConfig()
                .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
                .getEnabledModules())
            .map(s -> s.substring(0,1).toUpperCase() + s.substring(1))      // Capitalize first letter
            .sort()                                                         // Sort module categories alphabetically
            .concatWith(Flux.fromIterable(baseCategories).sort())           // Add in the base categories next
            .map("\n"::concat)                                              // Add newline before each item
            .collect(StringBuilder::new, StringBuilder::append)             // Append all items into a StringBuilder
            .map(StringBuilder::toString)                                   // Convert to String
            .filter(not(String::isBlank))                                   // Ensure it isn't blank
            .map(s -> String.format(CAT_LIST_MSG, s, ctx.getPrefix()))      // Format
            .defaultIfEmpty("There are no categories... Weird.")            // This shouldn't happen
            .flatMap(ctx::sendReply);
    }

    private static Collection<GuildModuleCommand<GuildModule>> getCommands(ModuleHandler moduleHandler){
        return moduleHandler.getGuildModuleCmds().values();
    }
}
