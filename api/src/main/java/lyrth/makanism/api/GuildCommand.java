package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import reactor.core.publisher.Mono;

public abstract class GuildCommand extends Command {

    @Override
    public boolean isGuildOnly() {
        return true;
    }

    public Mono<Boolean> allows(Member author){
        return this.getPerms().allows(author);
    }

}
