package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.*;

@CommandInfo()
public abstract class Command {

    private final CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);

    public String getName(){
        return commandInfo.name().equals("\0") ? this.getClass().getSimpleName().toLowerCase() : commandInfo.name();
    }

    public String[] getAliases(){
        return commandInfo.aliases();
    }

    public AccessLevel getPerms(){
        // TODO: complain when there are perms for bot commands
        return commandInfo.accessLevel();
    }

    public String getCategory(){
        return commandInfo.category();
    }

    public String getDesc(){
        return commandInfo.desc();
    }

    public String getUsage(){
        return commandInfo.usage().equals("\0") ? getName() : commandInfo.usage();
    }

    public int getMinArgs(){
        return commandInfo.minArgs();
    }

    public String getFormattedUsage(){
        return getUsage().replaceAll("([(\\[])","`$1")
            .replaceAll("([)\\]])","$1`");
    }

    public abstract boolean isGuildOnly();

    public Mono<Boolean> allows(@Nullable User author){
        return this.getPerms().allows(author);
    }

    public Mono<Boolean> allows(@Nullable Member author){
        return this.getPerms().allows(author);
    }

    // Can be an empty Mono, also prevents guild commands being run in dms. T: Allowed, F: Perm blocked, Empty: Non-user/GuildCommand in DM
    public Mono<Boolean> allows(@Nullable Member author, @Nullable User fallback){
        if (this instanceof GuildCommand)
            return this.getPerms().allows(author);
        else
            return this.getPerms().allows(fallback);
    }

    public abstract Mono<Void> execute(CommandCtx e);

}
