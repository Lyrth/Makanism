package lyrth.makanism.common.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public class FuncUtil {
    public static <T> Function<T, T> identity() {
        return t -> t;
    }

    public static <T> Function<T, T> it() {
        return t -> (T) t;
    }

    public static <T extends Boolean> Predicate<T> itself(){
        return b -> b;
    }

    public static <T> Consumer<T> noop(){
        return t -> {};
    }

    public static Function<Boolean, Boolean> not(){
        return b -> !b;
    }
}
