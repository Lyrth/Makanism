package lyrth.makanism.api;

import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.User;
import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.AccessLevel;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@CommandInfo()
public abstract class Command {         // TODO: subCommands and help (?)

    private final CommandInfo commandInfo = this.getClass().getAnnotation(CommandInfo.class);
    private static final Set<String> categories = new HashSet<>();

    {
        if (!commandInfo.category().equals("\0") || this.getParentModuleName().isEmpty()) { // non-module
            categories.add(this.getCategory());

        }
    }


    public String getName(){
        return commandInfo.name().equals("\0") ? this.getClass().getSimpleName() : commandInfo.name();
    }

    public String[] getAliases(){
        return commandInfo.aliases();
    }

    public AccessLevel getPerms(){
        return commandInfo.accessLevel();
    }

    // Uppercase initial.
    public String getCategory(){
        if (commandInfo.category().equals("\0")){       // auto-generate
            if (!this.getParentModuleName().isEmpty()){     // is a module command?
                return this.getParentModuleName();
            } else {                                        // generate from package name
                String packageName = this.getClass().getPackageName();
                if (packageName.endsWith(".commands")){     // not in any subpackage
                    return "General";
                } else {                                    // is in subpackage, get its name and capitalize
                    String subpackage = packageName.substring(packageName.lastIndexOf(".") + 1);
                    return subpackage.substring(0,1).toUpperCase() + subpackage.substring(1);
                }
            }
        } else {
            return commandInfo.category();              // information exists
        }
    }

    public Set<String> getAllCategories(){
        return Collections.unmodifiableSet(categories);
    }

    // Some parsed strings:
    //  ${prefix} : replaced with the guild/bot prefix
    public String getDesc(CommandCtx ctx){
        return commandInfo.desc()
            .replace("${prefix}", ctx.getPrefix())
            ;
    }

    public String getUsage(){       // returns a string without the prefix.
        return getName() + " " + commandInfo.usage();
    }

    // Returns the name of the parent module with proper capitalization
    // Is empty if not module command (or just not specified for non-GuildModule commands)
    public String getParentModuleName(){
        return commandInfo.parentModule().equals(IModule.class) ?
            "" :
            commandInfo.parentModule().getSimpleName();
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
