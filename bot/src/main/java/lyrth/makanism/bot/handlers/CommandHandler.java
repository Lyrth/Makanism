package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import lyrth.makanism.api.Command;
import lyrth.makanism.api.CommandEvent;
import lyrth.makanism.bot.commands.CheckPerms;
import lyrth.makanism.bot.commands.Mrawr;
import lyrth.makanism.bot.commands.Ping;
import lyrth.makanism.common.util.reactor.Jump;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

import java.util.*;

import static lyrth.makanism.common.util.FuncUtil.itself;

public class CommandHandler {
    private static final Logger log = LoggerFactory.getLogger(CommandHandler.class);

    public static Mono<Void> handle(GatewayDiscordClient client){

        LinkedHashSet<Command> commands = new LinkedHashSet<>(Arrays.asList(
            new Ping(),
            new Mrawr(),
            new CheckPerms()
        ));

        try (ScanResult scanResult = new ClassGraph().enableClassInfo()
            .whitelistPackages("lyrth.makanism.bot.commands").scan()){  // todo variable package names
            List<String> packageList = new LinkedList<>(scanResult
                .getSubclasses("lyrth.makanism.api.Command")
                .getNames());
            packageList.remove("lyrth.makanism.api.BotCommand");
            packageList.remove("lyrth.makanism.api.GuildCommand");
            commands.stream().map(cmd -> cmd.getClass().getName()).forEach(packageList::remove);
            if (packageList.size() > 0){
                log.warn("{} count mismatch, {} unused", CommandHandler.class.getName(), packageList.size());
                packageList.forEach(log::debug);
            }
        }

        String prefix = ";";  // TODO: LOAD

        return client.on(MessageCreateEvent.class)
            .map(CommandEvent::fromMsgEvent)
            .filter(e -> !e.getContent().isEmpty())
            .filter(e -> e.getContent().startsWith(prefix))
            .flatMap(e -> {
                String invokedName = e.getArgs().get(0).replaceFirst(prefix, "").toLowerCase();
                Optional<Command> command = commands.stream()
                    .filter(cmd -> cmd.getAliases().contains(invokedName))
                    .findFirst();
                return Mono.justOrEmpty(command)
                    .flatMap(cmd -> cmd.allows(e.getMember().orElse(null), e.getUser().orElse(null))
                        .switchIfEmpty(Jump.to1())  // Empty: -1 Non-user/GuildCommand in DM
                        .filter(itself())           // True :  0 Allowed
                        .switchIfEmpty(Jump.to2())  // False:  1 Not allowed
                        .flatMap($ -> cmd.execute(e).thenReturn(0))
                        .onErrorReturn(Jump::from1, -1)
                        .onErrorResume(Jump::from2, $ -> e.getChannel().flatMap(ch -> ch.createMessage("Not allowed!")).thenReturn(1))
                    )
                    .defaultIfEmpty(-1);  // -1 also: no command with that name
            })
            //.doOnNext(i -> log.debug("perm {}", i))
            .then();
    }

}
