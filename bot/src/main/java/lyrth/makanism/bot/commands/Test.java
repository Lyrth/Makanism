package lyrth.makanism.bot.commands;

import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.CommandEvent;
import reactor.core.publisher.Mono;

public class Test extends BotCommand {
    @Override
    public Mono<Void> execute(CommandEvent e) {
        return Mono.empty();
    }
}
