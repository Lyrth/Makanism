package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lyrth.makanism.api.Command;
import lyrth.makanism.api.CommandCtx;
import lyrth.makanism.bot.commands.CheckPerms;
import lyrth.makanism.bot.commands.Mrawr;
import lyrth.makanism.bot.commands.Ping;
import lyrth.makanism.bot.commands.SetPrefix;
import lyrth.makanism.bot.util.BotProps;
import lyrth.makanism.common.util.file.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Stream;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    public static Mono<Void> handle(GatewayDiscordClient client, BotConfig config, BotProps props){

        // Unmodifiable, no need for concurrent (Module commands need to be)
        // todo probably just ServiceLoader
        HashMap<String, Command> commands = Stream.of(

            new Ping(),
            new Mrawr(),
            new CheckPerms(),
            new SetPrefix()

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
            .filter(event -> !event.getMessage().getContent().isEmpty()     // Empty check
                                && checkPrefix(event, config))              // Prefix check
            .flatMap(event -> {                                             // Command check
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
                if (invokedName.isEmpty()) return Mono.empty();   // Some reason a user just sent a prefix lol.
                Command command = commands.get(invokedName.toLowerCase());
                if (command == null) return Mono.empty();       // Command not found: empty. Todo: delegate to ModuleHandler

                return command
                    .allows(event.getMember().orElse(null), event.getMessage().getAuthor().orElse(null))
                    .flatMap(allowed -> allowed ?
                        command.execute(CommandCtx.from(event, config, invokedName.toLowerCase())) :
                        event.getMessage().getChannel().flatMap(ch -> ch.createMessage("You are not allowed to run this!")).then()
                    );
            })
            .then();   // no u
    }


    private static boolean checkPrefix(MessageCreateEvent event, BotConfig config){    // just checks prefix
        if (event.getGuildId().isPresent())  // Guild
            return event.getMessage().getContent().startsWith(config.getGuildConfig(event.getGuildId().get()).getPrefix()) ||
                event.getMessage().getContent().startsWith("<@!" + config.getBotId().asString() + "> ") ||
                event.getMessage().getContent().startsWith("<@" + config.getBotId().asString() + "> ");
        else  // DM
            return event.getMessage().getContent().startsWith(config.getDefaultPrefix());   // not gonna check mention in dms lol
    }

}
