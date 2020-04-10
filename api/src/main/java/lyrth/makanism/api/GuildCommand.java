package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

public abstract class GuildCommand extends Command {

    @Override
    public boolean isGuildOnly() {
        return true;
    }

}
