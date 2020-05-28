package lyrth.makanism.bot.commands.admin;

import discord4j.common.util.Snowflake;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;

import static java.util.function.Predicate.not;

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
            return Flux.fromIterable(
                    ctx.getBotConfig()
                        .getGuildConfig(ctx.getGuildId().orElse(Snowflake.of(0L)))
                        .getEnabledModules())
                .sort()
                .map("\n"::concat)
                .collect(StringBuilder::new, StringBuilder::append)
                .map(StringBuilder::toString)
                .filter(not(String::isBlank))
                .map(s -> String.format(MODULE_LIST_MSG, s))
                .defaultIfEmpty("There are no enabled modules.")
                .flatMap(ctx::sendReply);
        }

        // if first arg isn't enable/disable, or the arg is just enable/disable
        if (!ctx.getArgs().matchesAt(1, "enable|disable") || ctx.getArgs().getCount() == 1)
            return ctx.sendReply("Invalid args.");

        return Mono.just(ctx.getArg(2))
            .map(moduleName -> ctx.getArgs().equalsAt(1, "enable") ?
                ctx.getBotConfig().enableGuildModule(moduleName,ctx.getGuildId().orElse(Snowflake.of(0L))) :
                ctx.getBotConfig().disableGuildModule(moduleName,ctx.getGuildId().orElse(Snowflake.of(0L)))
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(bool -> bool ?
                String.format(SUCCESS_MESSAGE, ctx.getArg(2).toLowerCase(), ctx.getArg(1).toLowerCase()) :
                String.format(ALREADY_MESSAGE, ctx.getArg(2).toLowerCase(), ctx.getArg(1).toLowerCase())
            )
            .defaultIfEmpty("Module not found.")
            .flatMap(ctx::sendReply);
    }
}