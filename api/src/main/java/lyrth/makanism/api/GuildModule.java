package lyrth.makanism.api;

import discord4j.core.GatewayDiscordClient;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.api.annotation.GuildModuleInfo;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.config.GuildConfig;
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Mono;

import java.util.concurrent.ConcurrentHashMap;

@GuildModuleInfo()
public abstract class GuildModule implements IModule {

    private final GuildModuleInfo moduleInfo = this.getClass().getAnnotation(GuildModuleInfo.class);

    protected final ConcurrentHashMap<Snowflake, GuildConfig> enabledGuilds;  // enforce concurrency

    protected BotConfig config;
    protected GatewayDiscordClient client;

    DirectProcessor<GuildConfig> registerProcessor;
    DirectProcessor<GuildConfig> removeProcessor;

    // 0-arg constructor for ServiceLoader, do not use by itself?
    public GuildModule(){
        registerProcessor = DirectProcessor.create();
        removeProcessor = DirectProcessor.create();
        enabledGuilds = new ConcurrentHashMap<>();
    }

    @Override
    public Mono<Void> init(GatewayDiscordClient client, BotConfig config){
        this.config = config;
        this.client = client;
        return Mono.when(
            registerProcessor.flatMap(this::onRegister),
            removeProcessor.flatMap(this::onRemove),
            initModule()
        );
    }

    // Called once!  // Probably just a filter and not ModuleHandler. TO/nope/DO: ClassGraph????
    protected abstract Mono<Void> initModule();  // Should return a void Mono subscribing to the GuildModule's functions
    // on(*).filter(guildInEnabledGuild).(useGuildHere)

    protected Mono<Void> onRegister(GuildConfig config) {   // callback when module enabled
        return Mono.empty();
    }

    protected Mono<Void> onRemove(GuildConfig config) {     // callback when module disabled
        return Mono.empty();
    }


    public boolean register(GuildConfig config) {
        if (!enabledGuilds.containsKey(config.getId())) {
            enabledGuilds.put(config.getId(), config);
            registerProcessor.onNext(config);
            return true;    // successfully enabled
        }
        return false;   // already enabled
    }

    public boolean remove(Snowflake guildId) {
        if (enabledGuilds.containsKey(guildId)) {
            removeProcessor.onNext(enabledGuilds.remove(guildId));
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
    public Class<GuildModuleCommand<GuildModule>>[] getModuleCommands(){
        return (Class<GuildModuleCommand<GuildModule>>[]) moduleInfo.commands();
    }
}
