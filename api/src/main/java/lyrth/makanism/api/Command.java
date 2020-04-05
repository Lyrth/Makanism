package lyrth.makanism.api;

import discord4j.core.object.entity.User;
import lyrth.makanism.api.annotation.CommandInfo;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@CommandInfo()
public abstract class Command {

    private final CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);

    public String getName(){
        return commandInfo.name().equals("\0") ? this.getClass().getSimpleName().toLowerCase() : commandInfo.name();
    }

    public List<String> getAliases(){
        List<String> list = Arrays.asList(commandInfo.aliases());
        list.add(getName());
        return Collections.unmodifiableList(list);
    }

    public PermissionLevel getPerms(){
        return commandInfo.perms();
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

    public Mono<Boolean> allows(User author){
        return this.getPerms().allows(author);
    }

    public abstract Mono<?> execute(CommandEvent e);

}
