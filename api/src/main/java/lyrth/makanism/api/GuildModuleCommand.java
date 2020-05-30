package lyrth.makanism.api;

import lyrth.makanism.api.annotation.CommandInfo;
import lyrth.makanism.api.object.CommandCtx;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

@CommandInfo()
public abstract class GuildModuleCommand<M extends GuildModule<?>> extends GuildCommand {

    @Override
    public String getParentModuleName(){
        String clazz = getParentModuleClass().getTypeName();
        return clazz.substring(clazz.lastIndexOf('.') + 1);     // possible breakage?
    }

    @SuppressWarnings("unchecked")
    public Class<M> getParentModuleClass(){
        return (Class<M>) ((ParameterizedType) this.getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];
    }

    public final Mono<?> execute(CommandCtx ctx) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("Don't use this.");
    }

    public abstract Mono<?> execute(CommandCtx ctx, M module);

}
