package lyrth.makanism.api;

import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

@CommandInfo()
public abstract class GuildModuleCommand<M extends GuildModule> extends GuildCommand {

    @SuppressWarnings("unchecked")
    @Override
    public String getParentModuleName(){
        String clazz = (
            (Class<M>) ((ParameterizedType) this.getClass().getGenericSuperclass())
                .getActualTypeArguments()[0])
            .getTypeName();
        return clazz.substring(clazz.lastIndexOf('.') + 1);     // possible breakage?
    }

    public final Mono<?> execute(CommandCtx ctx) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Don't use this.");
    }

    public abstract Mono<?> execute(CommandCtx ctx, M module);

}
