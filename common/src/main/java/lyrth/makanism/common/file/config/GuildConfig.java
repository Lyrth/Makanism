package lyrth.makanism.common.file.config;

import com.google.gson.reflect.TypeToken;
import discord4j.common.util.Snowflake;
import lyrth.makanism.common.file.SourceProvider;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

public class GuildConfig {

    private transient static final TypeToken<HashMap<String, ModuleConfig>> MOD_CONFIGS_TYPE = new TypeToken<>(){};
    private transient final HashMap<String, ModuleConfig> moduleConfigs = new HashMap<>();

    private transient SourceProvider source;
    private transient BotConfig botConfig;

    private HashSet<String> enabledModules;
    // private HashSet<String> enabledSubModules;      // submodules has module.submodule notation, no effect when parent module is disabled
    private HashSet<String> commandBlacklist;
    private HashSet<String> commandWhitelist;    // Whitelist has precedence.

    private Snowflake guildId;
    private String prefix;


    // Read from resource
    public static Mono<GuildConfig> load(Snowflake guildId, SourceProvider source, BotConfig botConfig){
        String path = "guilds/" + guildId.asString() + "/";
        return source.read(path + "guild", GuildConfig.class)
            .map(config -> config.setSource(source).setBotConfig(botConfig))
            .flatMap(c -> c.readModuleConfigs(path))
            .map(config ->
                config.setId(config.getId() == null ?
                        guildId :
                        config.getId())
                    .setPrefix(config.getPrefix() == null ?
                        botConfig.getDefaultPrefix() :
                        config.getPrefix())
                    .setCommandWhitelist(config.getCommandWhitelist() == null ?
                        new HashSet<>() :
                        config.getCommandWhitelist())
                    .setCommandBlacklist(config.getCommandBlacklist() == null ?
                        new HashSet<>() :
                        config.getCommandBlacklist())
                    .setEnabledModules(config.getEnabledModules() == null ?
                        new HashSet<>() :
                        config.getEnabledModules())
            );
    }

    // Write to resource
    public Mono<GuildConfig> update(){
        String path = "guilds/" + guildId.asString() + "/";
        return Mono.when(
            source.write(path + "guild", this),
            writeModuleConfigs(path, moduleConfigs)
        ).thenReturn(this);
    }

    @SuppressWarnings("ReactiveStreamsNullableInLambdaInTransform")
    private Mono<GuildConfig> readModuleConfigs(String path){
        return Flux.fromIterable(botConfig.getModuleHandler().getModuleNames())
            .map(String::toLowerCase)
            .flatMap(moduleName ->  // createIfMissing false: don't create folders for unused modules
                source.read(path + moduleName + "/config", botConfig.getModuleHandler().getModuleConfigClass(moduleName), false)
                    .doOnNext(config -> moduleConfigs.put(moduleName, config)))
            .then()
            .thenReturn(this);
    }

    private Mono<Void> writeModuleConfigs(String path, Map<String, ModuleConfig> configs){
        return Flux.fromIterable(configs.entrySet())
            .flatMap(entry ->
                source.write(path + entry.getKey() + "/config", entry.getValue()))
            .then();
    }


    public GuildConfig setPrefix(String prefix) {
        this.prefix = prefix.replace(' ', '_');
        return this;
    }

    public Optional<Boolean> enableModule(String moduleName){
        return botConfig.enableGuildModule(moduleName, this.guildId);
    }

    public Optional<Boolean> disableModule(String moduleName){
        return botConfig.disableGuildModule(moduleName, this.guildId);
    }

    public <T extends ModuleConfig> void putModuleConfig(String moduleName, T config){
        moduleConfigs.put(moduleName, config);
    }

    private GuildConfig setId(Snowflake id){
        this.guildId = id;
        return this;
    }

    private GuildConfig setEnabledModules(HashSet<String> enabledModules) {
        this.enabledModules = enabledModules;
        return this;
    }

    private GuildConfig setCommandBlacklist(HashSet<String> commandBlacklist) {
        this.commandBlacklist = commandBlacklist;
        return this;
    }

    private GuildConfig setCommandWhitelist(HashSet<String> commandWhitelist) {
        this.commandWhitelist = commandWhitelist;
        return this;
    }

    private GuildConfig setSource(SourceProvider source){
        this.source = source;
        return this;
    }

    private GuildConfig setBotConfig(BotConfig botConfig){
        this.botConfig = botConfig;
        return this;
    }

    @Nullable
    public <T extends ModuleConfig> T getModuleConfig(String moduleName, Class<T> type){
        return type.cast(moduleConfigs.get(moduleName));
    }

    /*
    private GuildConfig setModulesSettings(HashMap<String, HashMap<String,String>> map){
        this.modulesSettings = map;
        return this;
    }

    public HashMap<String, HashMap<String, String>> getModulesSettings() {
        return modulesSettings;
    }

    // module name is lowercase simple class name
    public HashMap<String, String> getModuleSettings(String moduleName){
        modulesSettings.computeIfAbsent(moduleName, k -> new HashMap<>());
        return modulesSettings.get(moduleName);
    }

    @Nullable
    public String getModuleSetting(String moduleName, String settingName){
        return getModuleSettings(moduleName).get(settingName);
    }

    public GuildConfig setModuleSetting(String moduleName, String settingName, String value){   //
        getModuleSettings(moduleName).put(settingName, value);
        return this;
    }
     */

    // Lowercase keys.
    public HashSet<String> getEnabledModules() {
        return enabledModules;
    }

    public HashSet<String> getCommandBlacklist() {
        return commandBlacklist;
    }

    public HashSet<String> getCommandWhitelist() {
        return commandWhitelist;
    }

    public String getPrefix() {
        return prefix;
    }

    public Snowflake getId() {
        return guildId;
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }

    public HashMap<String, ModuleConfig> getModuleConfigs() {
        return moduleConfigs;
    }
}
