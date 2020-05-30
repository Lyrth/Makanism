package lyrth.makanism.bot.commands.general;

import discord4j.core.spec.EmbedCreateSpec;
import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.Command;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.bot.handlers.CommandHandler;
import lyrth.makanism.bot.handlers.ModuleHandler;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.function.Consumer;


@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Shows some help?",
    usage = "[<command name>]"
)
public class Help extends BotCommand {

    @Override
    public Mono<?> execute(CommandCtx ctx) {

        // A: no args: list the command categories. would essentially be equivalent to CommandsCmd
        if (ctx.getArgs().isEmpty()){       // return list of categories
            return CommandsCmd.sendCategories(ctx, getAllCategories());
        }

        return Mono.justOrEmpty(    // smol todo: fuzzy search
                getCommand(ctx.getArg(1), ((ModuleHandler) ctx.getBotConfig().getModuleHandler())))
            .map(cmd -> buildHelp(cmd, ctx))
            .switchIfEmpty(ctx.sendReply("Command not found."))
            .flatMap(ctx::sendReply);
    }

    private static Optional<Command> getCommand(String name, ModuleHandler moduleHandler){
        return Optional.ofNullable(CommandHandler.getCommands().get(name.toLowerCase()))
            .or(() -> Optional.ofNullable(moduleHandler.getGuildModuleCmds().get(name.toLowerCase())));
    }

    private static Consumer<EmbedCreateSpec> buildHelp(Command command, CommandCtx ctx){
        return embed -> {
            embed.setTitle(command.getName())
                .setDescription(command.getDesc())
                .addField("Usage:", ctx.getPrefix() + command.getUsage().toLowerCase(), true);
            if (command.getAliases().length > 0)
                embed.addField("Aliases:", String.join(", ", command.getAliases()), true);
            if (!command.getParentModuleName().isEmpty())
                embed.addField("From module:", command.getParentModuleName(), true);
            else
                embed.addField("Category:", command.getCategory(), true);
            embed.addField("Access level:", command.getPerms().name(), true);
            embed.setFooter(
                    "[<...>] - arg is optional \n" +
                    "(<...>) - arg is required \n" +
                    "aaa|bbb - arg is either aaa or bbb \n" +
                    "help : " + ctx.getAuthorIdText(),
                null);
        };
    }
}
