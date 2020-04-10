package lyrth.makanism.bot.commands;

import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.CommandEvent;
import lyrth.makanism.api.AccessLevel;
import lyrth.makanism.api.GuildCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(accessLevel = AccessLevel.OWNER)
public class Mrawr extends GuildCommand {
    private static final Logger log = LoggerFactory.getLogger(Mrawr.class);

    @Override
    public Mono<Void> execute(CommandEvent e) {
        return e.getMessage().getChannel()
            .flatMap(channel -> channel.createMessage("mrrawrrf!"))
            .then();
    }
}
