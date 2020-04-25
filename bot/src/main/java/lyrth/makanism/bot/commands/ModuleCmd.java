package lyrth.makanism.bot.commands;

import discord4j.rest.util.Snowflake;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

import java.util.Optional;

@CommandInfo(
    name = "Module",
    accessLevel = AccessLevel.OWNER
)
public class ModuleCmd extends GuildCommand {   // TODO : fix this mess

    @Override
    public Mono<Void> execute(CommandCtx ctx) {
        if (!(ctx.getArgs().get(1).toLowerCase().equals("enable") ||
            ctx.getArgs().get(1).toLowerCase().equals("disable")) || ctx.getArgs().getCount() < 3)
            return ctx.sendReply("Invalid args.");
        return Mono.just(ctx.getArgs().get(2))
            .map(moduleName -> ctx.getArgs().get(1).toLowerCase().equals("enable") ?
                ctx.getBotConfig().enableGuildModule(moduleName,ctx.getGuildId().orElse(Snowflake.of(0L))) :
                ctx.getBotConfig().disableGuildModule(moduleName,ctx.getGuildId().orElse(Snowflake.of(0L)))
            )
            .filter(Optional::isPresent)
            .map(Optional::get)
            .map(bool -> bool ?
                ctx.getArgs().get(2).toLowerCase() + " " + ctx.getArgs().get(1).toLowerCase() + "d." :
                ctx.getArgs().get(2).toLowerCase() + " already " + ctx.getArgs().get(1).toLowerCase() + "d."
            )
            .defaultIfEmpty("Module not found.")
            .flatMap(ctx::sendReply);
    }
}
