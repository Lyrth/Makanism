package lyrth.makanism.api.annotation;

import lyrth.makanism.api.GuildModuleCommand;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface GuildModuleInfo {
    String name() default "\0";
    String desc() default "";
    Class<? extends GuildModuleCommand<?>>[] commands() default {};
}
