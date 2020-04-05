package lyrth.makanism.api.annotation;

import lyrth.makanism.api.PermissionLevel;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CommandInfo {
    String name() default "\0";
    String[] aliases() default {};  // Excludes the command name. Is included in abstract
    boolean isGuildOnly() default false;
    PermissionLevel perms() default PermissionLevel.OWNER;
    String category() default "General";
    String desc() default "";
    String usage() default "\0";
    int minArgs() default 0;
}
