package lyrth.makanism.api.annotation;

import lyrth.makanism.api.IModule;
import lyrth.makanism.api.object.AccessLevel;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CommandInfo {
    String name() default "\0";     // Automatically generated from class name if not set.
    String[] aliases() default {};  // Exclude the actual command name (name field).
    AccessLevel accessLevel() default AccessLevel.OWNER;
    String category() default "\0"; // Automatic, from (1) module name if GuildModuleCommand, (2) package name commands.*, (3) General if just placed on commands package
    String desc() default "";
    String usage() default "";    // Exclude the command name. If not set, defaults to no-arg command usage: just the name of the command or don't even display it.
    // ^ TODO: as array? Tooltips also.
    Class<? extends IModule> parentModule() default IModule.class;  // Automatic for GuildModuleCommands
}

// TODO: long description/manual