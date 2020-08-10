package lyrth.makanism.bot.commands.owner;

import lyrth.makanism.api.BotCommand;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

@CommandInfo(
    accessLevel = AccessLevel.OWNER,
    desc = "Garbage collection command. Is meant to be used for loaded command"
)
public class Gc extends BotCommand {
    private static final Logger log = LoggerFactory.getLogger(Gc.class);

    @Override
    public Mono<?> execute(CommandCtx ctx) {
        return Mono.fromRunnable(System::gc).then(ctx.sendReply("Done."));
    }
}
