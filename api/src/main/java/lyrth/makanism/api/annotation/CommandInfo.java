package lyrth.makanism.api.annotation;

import lyrth.makanism.api.IModule;
import lyrth.makanism.api.object.AccessLevel;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface CommandInfo {     // TODO: Make it so that this is only applied to command classes
    String name() default "\0";
    String[] aliases() default {};  // Not including the command name.
    AccessLevel accessLevel() default AccessLevel.OWNER;
    String category() default "\0";
    String desc() default "";
    String usage() default "\0";
    Class<? extends IModule> parentModule() default IModule.class;
}
