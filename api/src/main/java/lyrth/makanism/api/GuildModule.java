package lyrth.makanism.api;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.config.GuildConfig;
import lyrth.makanism.common.file.config.ModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.concurrent.ConcurrentHashMap;

@GuildModuleInfo()
public abstract class GuildModule<T extends ModuleConfig> implements IModule {
    private static final Logger log = LoggerFactory.getLogger(GuildModule.class);

    private final GuildModuleInfo moduleInfo = this.getClass().getAnnotation(GuildModuleInfo.class);

    protected final ConcurrentHashMap<Snowflake, GuildConfig> enabledGuilds = new ConcurrentHashMap<>();  // enforce concurrency

    protected BotConfig config;
    protected GatewayDiscordClient client;

    private final DirectProcessor<GuildConfig> registerProcessor = DirectProcessor.create();
    private final DirectProcessor<GuildConfig> removeProcessor   = DirectProcessor.create();
    private final FluxSink<GuildConfig> registerSink = registerProcessor.sink(FluxSink.OverflowStrategy.BUFFER);
    private final FluxSink<GuildConfig> removeSink   = removeProcessor  .sink(FluxSink.OverflowStrategy.BUFFER);

    @Override
    public Mono<?> init(GatewayDiscordClient client, BotConfig config){
        this.config = config;
        this.client = client;
        return Mono.when(
            registerProcessor.flatMap(this::onRegister),
            removeProcessor.flatMap(this::onRemove),
            initModule()
        );
    }

    // Called once!  // Probably just a filter and not ModuleHandler. TO/nope/DO: ClassGraph????
    protected abstract Mono<?> initModule();  // Should return a void Mono subscribing to the GuildModule's functions
    // on(*).filter(guildInEnabledGuild).(useGuildHere)

    protected Mono<?> onRegister(GuildConfig config) {   // callback when module enabled
        return Mono.empty();
    }

    protected Mono<?> onRemove(GuildConfig config) {     // callback when module disabled
        return Mono.empty();
    }

    public Mono<?> uninit(){
        return Flux.fromIterable(enabledGuilds.keySet())
            .flatMap(id -> onRemove(enabledGuilds.get(id)))
            .then();
    }


    public boolean register(GuildConfig config) {
        if (!enabledGuilds.containsKey(config.getId())) {
            enabledGuilds.put(config.getId(), config);
            registerSink.next(config);
            return true;    // successfully enabled
        }
        return false;   // already enabled
    }

    public boolean remove(Snowflake guildId) {
        if (enabledGuilds.containsKey(guildId)) {
            removeSink.next(enabledGuilds.remove(guildId));
            return true;    // successfully disabled
        }
        return false;   // already disabled
    }

    public boolean isEnabledFor(Snowflake guildId){
        return enabledGuilds.containsKey(guildId);
    }

    public String getName(){
        return moduleInfo.name().equals("\0") ? this.getClass().getSimpleName() : moduleInfo.name();
    }

    public String getDesc(){
        return moduleInfo.desc();
    }

    @SuppressWarnings("unchecked")      // because this works fine
    public Class<GuildModuleCommand<GuildModule<?>>>[] getModuleCommands(){
        return (Class<GuildModuleCommand<GuildModule<?>>>[]) moduleInfo.commands();
    }

    public T getConfig(Snowflake guildId){
        if (!isEnabledFor(guildId)) return null;

        T config = enabledGuilds.get(guildId).getModuleConfig(this.getName().toLowerCase(), getConfigClass());
        if (config == null){
            T newConfig;
            try {
                newConfig = getConfigClass().getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                log.error("Error instantiating {} for {}! {}", getConfigClass().getSimpleName(), this.getName(), e.getMessage());
                return null;
            }
            enabledGuilds.get(guildId).putModuleConfig(this.getName().toLowerCase(), newConfig);
            return newConfig;
        }
        return config;
    }

    public T getConfig(CommandCtx ctx){
        return getConfig(ctx.getGuildId().orElse(Snowflake.of(0L)));
    }

    @SuppressWarnings("unchecked")
    public Class<T> getConfigClass(){
        return (Class<T>) ((ParameterizedType) this.getClass().getGenericSuperclass())
            .getActualTypeArguments()[0];
    }
}
