package lyrth.makanism.api.annotation;

import lyrth.makanism.api.Command;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface BotModuleInfo {
    String name() default "\0";
    String desc() default "";
    Class<Command>[] commands() default {};
}
