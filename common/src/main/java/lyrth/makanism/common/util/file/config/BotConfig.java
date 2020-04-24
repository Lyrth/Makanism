package lyrth.makanism.common.util.file.config;

import discord4j.rest.util.Snowflake;
import lyrth.makanism.common.util.file.IModuleHandler;
import lyrth.makanism.common.util.file.Props;
import lyrth.makanism.common.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;

public class BotConfig {
    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    private transient HashMap<Snowflake, GuildConfig> guildConfigs;

    private transient SourceProvider source;
    private transient Props props;

    private transient IModuleHandler moduleHandler;

    private String defaultPrefix;    // only modifiable in json
    private Snowflake botId;
    private Snowflake ownerId;

    private HashSet<String> disabledCommands;   // global blacklist


    public static Mono<BotConfig> load(SourceProvider source, HashMap<Snowflake, GuildConfig> guildConfigs, Props props, IModuleHandler moduleHandler){
        String path = "bot/bot";
        return source.read(path, BotConfig.class).map(config ->
            config.setSource(source)
                .setProps(props)
                .setGuildConfigs(guildConfigs)
                .setModuleHandler(moduleHandler)
                .setDefaultPrefix(config.getDefaultPrefix() == null ?
                    props.get("bot.prefix") :
                    config.getDefaultPrefix())
                .setDisabledCommands(config.getDisabledCommands() == null ?
                    new HashSet<>() :
                    config.getDisabledCommands())
        );
    }

    // Write to resource
    public Mono<BotConfig> update(){
        String path = "bot/bot";
        return source.write(path, this).thenReturn(this);
    }

    public Mono<Void> saveAll(){
        return update()
            .and(Flux.fromIterable(guildConfigs.values())
                .flatMap(GuildConfig::update).then());
    }

    public Optional<Boolean> enableGuildModule(String moduleName, Snowflake guildId){
        return Optional.of(guildConfigs.get(guildId))   // this should never be null
            .flatMap(guildConfig ->
                moduleHandler.enable(moduleName, guildConfig).map(b -> {
                    if (b) guildConfig.addEnabledModule(moduleName);
                    return b;
                })
            );
    }

    public Optional<Boolean> disableGuildModule(String moduleName, Snowflake guildId){
        return Optional.of(guildConfigs.get(guildId))   // this should never be null
            .flatMap(guildConfig ->
                moduleHandler.disable(moduleName, guildId).map(b -> {
                    if (b) guildConfig.removeEnabledModule(moduleName);
                    return b;
                })
            );
    }

    public BotConfig putGuildConfig(GuildConfig config){
        guildConfigs.put(config.getId(),config);
        return this;
    }

    public void setIds(Snowflake botId, Snowflake ownerId) {
        this.botId = botId;
        this.ownerId = ownerId;
    }

    private BotConfig setSource(SourceProvider source){
        this.source = source;
        return this;
    }

    private BotConfig setModuleHandler(IModuleHandler handler) {
        moduleHandler = handler;
        return this;
    }

    private BotConfig setProps(Props props) {
        this.props = props;
        return this;
    }

    private BotConfig setGuildConfigs(HashMap<Snowflake, GuildConfig> guildConfigs) {
        this.guildConfigs = guildConfigs;
        return this;
    }

    public BotConfig setDefaultPrefix(String defaultPrefix) {
        this.defaultPrefix = defaultPrefix.replace(' ', '_');
        return this;
    }

    public BotConfig setDisabledCommands(HashSet<String> disabledCommands) {
        this.disabledCommands = disabledCommands;
        return this;
    }

    public String getDefaultPrefix() {
        return defaultPrefix;
    }

    public Snowflake getBotId() {
        return botId;
    }

    public Snowflake getOwnerId() {
        return ownerId;
    }

    public HashSet<String> getDisabledCommands() {
        return disabledCommands;
    }

    public boolean isCommandDisabled(String name) {
        return disabledCommands.contains(name);
    }

    public GuildConfig getGuildConfig(Snowflake guildId) {
        return guildConfigs.get(guildId);
    }

    public SourceProvider getSourceProvider() {
        return source;
    }

    public Props getProps() {
        return props;
    }

    public IModuleHandler getModuleHandler(){
        return moduleHandler;
    }
}