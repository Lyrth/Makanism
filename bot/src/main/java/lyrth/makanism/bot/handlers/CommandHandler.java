package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.Command;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.bot.commands.*;
import lyrth.makanism.common.util.file.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    public static Mono<Void> handle(GatewayDiscordClient client, BotConfig config){

        // Not going to modify, no need for concurrent
        HashMap<String, Command> commands = Stream.of(

            new Ping(),
            new Mrawr(),
            new CheckPerms(),
            new SetPrefix(),
            new Echo(),
            new Sudo(),
            new UserInfo(),
            new ModuleCmd()

        ).collect(
            HashMap::new,
            (map, cmd) -> {
                map.putIfAbsent(cmd.getName().toLowerCase(), cmd);
                if (cmd.getAliases() != null)
                    for (String alias : cmd.getAliases()) map.putIfAbsent(alias.toLowerCase(), cmd);
            },
            (a,b) -> {}
        );

        // This part should be fast.
        return client.on(MessageCreateEvent.class)
            .filter(event -> !event.getMessage().getContent().isEmpty()                     // Empty check
                && !event.getMessage().getAuthor().map(User::isBot).orElse(true)            // Is bot?
                && checkPrefix(event, event.getMessage().getContent(), config))             // Prefix check
            .flatMap(event -> checkCommand(event, config, commands))                        // Command check
            .then();
    }

    private static boolean checkPrefix(MessageCreateEvent event, String content, BotConfig config){    // just checks prefix
        return event.getGuildId().map(guildId ->    // Guild
            content.startsWith(config.getGuildConfig(guildId).getPrefix()) ||
                content.startsWith("<@!" + config.getBotId().asString() + "> ") ||
                content.startsWith("<@" + config.getBotId().asString() + "> "))
            .orElseGet(() ->     // DMs
                content.startsWith(config.getDefaultPrefix()));     // not gonna check mention in dms lol
    }

    private static Mono<Void> checkCommand(MessageCreateEvent event, BotConfig config, Map<String, Command> commands){
        String[] words = event.getMessage().getContent().split("\\s+", 3);
        String second = words.length > 1 ? words[1] : "";
        boolean inGuild = event.getGuildId().isPresent();
        String invokedName;
        if (inGuild) {   // Guild
            if (words[0].equals("<@!" + config.getBotId().asString() + ">") ||
                words[0].equals("<@" + config.getBotId().asString() + ">"))         // @User command
                invokedName = second;
            else {                                                                  // ;command or ; command
                String prefix = config.getGuildConfig(event.getGuildId().get()).getPrefix();
                invokedName = words[0].equals(prefix) ? second : words[0].substring(prefix.length());
            }
        } else  invokedName = words[0].equals(config.getDefaultPrefix()) ?          // ;command or ; command
            second : words[0].substring(config.getDefaultPrefix().length());

        // Prefix only, or command disabled.
        if (invokedName.isEmpty() || config.isCommandDisabled(invokedName)) return Mono.empty();

        // TODO Command overrides (ModuleHandler checkForCommandOverride, then pass

        Command command = commands.get(invokedName.toLowerCase());
        if (command == null)            // Command not found: delegate to ModuleHandler
            return config.getModuleHandler().handleCommand(event,config,invokedName);

        return command    // todo check # of args, filter if guildCommand? (already filtered?)
            .allows(event.getMember().orElse(null), event.getMessage().getAuthor().orElse(null))
            .flatMap(allowed -> allowed ?
                command.execute(CommandCtx.from(event, config, invokedName.toLowerCase())) :
                event.getMessage().getChannel().flatMap(ch -> ch.createMessage("You are not allowed to run this!")).then()
            )
            .doOnError(t -> log.error("CAUgHt eWWoW!", t))
            .onErrorResume(t -> event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage("Oh no, an error has occurred! ``` " + t.toString() + "```")).then());
    }
}
