package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.http.client.ClientException;
import lyrth.makanism.api.Command;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.bot.commands.admin.CheckPerms;
import lyrth.makanism.bot.commands.admin.ModuleCmd;
import lyrth.makanism.bot.commands.admin.SetPrefix;
import lyrth.makanism.bot.commands.botstats.Ping;
import lyrth.makanism.bot.commands.general.CommandsCmd;
import lyrth.makanism.bot.commands.general.Echo;
import lyrth.makanism.bot.commands.general.Help;
import lyrth.makanism.bot.commands.general.Mrawr;
import lyrth.makanism.bot.commands.info.UserInfo;
import lyrth.makanism.bot.commands.owner.Sudo;
import lyrth.makanism.bot.commands.owner.Test;
import lyrth.makanism.common.file.config.BotConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    private static final String ERROR_MSG = "Oh no, an error has occurred! ```%s```";
    private static final String DISALLOWED_MSG = "You are not allowed to run this!";

    private static final Map<String, Command> commands;

    static {
        commands = Collections.unmodifiableMap(Stream.of(

            new Ping(),
            new Mrawr(),
            new CheckPerms(),
            new SetPrefix(),
            new Echo(),
            new Sudo(),
            new Test(),
            new UserInfo(),
            new ModuleCmd(),
            new Help(),
            new CommandsCmd()

        ).collect(
            HashMap::new,
            (map, cmd) -> {
                map.putIfAbsent(cmd.getName().toLowerCase(), cmd);
                if (cmd.getAliases() != null)
                    for (String alias : cmd.getAliases()) map.putIfAbsent(alias.toLowerCase(), cmd);
            },
            (a,b) -> {}
        ));
    }

    public static Mono<?> handle(GatewayDiscordClient client, BotConfig config){
        // This part should be fast.
        return client.on(MessageCreateEvent.class)
            .filter(event -> !event.getMessage().getContent().isEmpty()                     // Empty check
                && !event.getMessage().getAuthor().map(User::isBot).orElse(true)            // Is bot?
                && checkPrefix(event, event.getMessage().getContent(), config))             // Prefix check
            .flatMap(event -> checkCommand(event, config))                        // Command check
            .then();
    }

    private static Mono<?> checkCommand(MessageCreateEvent event, BotConfig config){
        String invokedName = getInvokedName(event, config);

        // Prefix only, or command disabled.
        if (invokedName.isEmpty() || config.isCommandDisabled(invokedName)) return Mono.empty();

        // TODO Command overrides (ModuleHandler checkForCommandOverride, then pass

        Command command = commands.get(invokedName.toLowerCase());
        if (command == null)            // Command not found: delegate to ModuleHandler
            return config.getModuleHandler().handleCommand(event,config,invokedName);

        return command
            .allows(event.getMember().orElse(null), event.getMessage().getAuthor().orElse(null))
            .flatMap(allowed -> allowed ?
                command.execute(CommandCtx.from(event, config, invokedName.toLowerCase())) :
                event.getMessage().getChannel().flatMap(ch -> ch.createMessage(DISALLOWED_MSG))
            )
            .doOnError(t -> log.error("CAUgHt eWWoW!", t))
            .onErrorResume(t -> event.getMessage().getChannel()
                .flatMap(ch -> sendError(t, ch)));
    }

    protected static boolean checkPrefix(MessageCreateEvent event, String content, BotConfig config){    // just checks prefix
        return event.getGuildId().map(guildId ->    // Guild
            content.startsWith(config.getGuildConfig(guildId).getPrefix()) ||
                content.startsWith("<@!" + config.getBotId().asString() + "> ") ||
                content.startsWith("<@" + config.getBotId().asString() + "> "))
            .orElseGet(() ->     // DMs
                content.startsWith(config.getDefaultPrefix()));     // not gonna check mention in dms lol
    }

    protected static String getInvokedName(MessageCreateEvent event, BotConfig config){
        String[] words = event.getMessage().getContent().split("\\s+", 3);
        String second = words.length > 1 ? words[1] : "";
        boolean inGuild = event.getGuildId().isPresent();
        if (inGuild) {   // Guild
            if (words[0].equals("<@!" + config.getBotId().asString() + ">") ||
                words[0].equals("<@" + config.getBotId().asString() + ">"))         // @User command
                return second;
            else {                                                                  // ;command or ; command
                String prefix = config.getGuildConfig(event.getGuildId().get()).getPrefix();
                return words[0].equals(prefix) ? second : words[0].substring(prefix.length());
            }
        } else  return words[0].equals(config.getDefaultPrefix()) ?          // ;command or ; command
            second : words[0].substring(config.getDefaultPrefix().length());
    }

    protected static Mono<?> sendError(Throwable t, MessageChannel channel){
        String errorMessage;
        if (t instanceof ClientException){
            ClientException ex = ((ClientException)t);
            errorMessage = ex.getStatus().toString() + ": " +
                ex.getErrorResponse().map(resp -> resp.getFields().get("message")).orElse("<no content>");
        } else {
            errorMessage = t.toString();
        }
        return channel.createMessage(String.format(ERROR_MSG, errorMessage));
    }

    // Lowercase keys.
    public static Map<String, Command> getCommands() {
        return commands;
    }
}
