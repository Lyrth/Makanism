package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.util.AccessLevel;
import lyrth.makanism.api.util.CommandCtx;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

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
        return commandInfo.category().equals("\0") ?
            (this.getParentModuleName().isEmpty() ? "General" : this.getParentModuleName()) :
            commandInfo.category();
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

    // Returns the name of the parent module with proper capitalization
    public String getParentModuleName(){
        return commandInfo.parentModule().getSimpleName().equals("IModule") ?
            "" : commandInfo.parentModule().getSimpleName();
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
    public Mono<Boolean> allows(@Nullable Member member, @Nullable User user){
        if (this instanceof GuildCommand)
            return this.getPerms().allows(member);
        else
            return this.getPerms().allows(user);
    }

    public abstract Mono<?> execute(CommandCtx e);
}
