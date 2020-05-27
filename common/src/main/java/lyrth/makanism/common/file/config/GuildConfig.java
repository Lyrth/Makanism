package lyrth.makanism.common.file.config;

import com.google.gson.reflect.TypeToken;
import discord4j.common.util.Snowflake;
import lyrth.makanism.common.file.Props;
import lyrth.makanism.common.file.SourceProvider;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class GuildConfig {    // TODO not null fields

    private transient HashMap<String, HashMap<String,String>> modulesSettings;  // <Module, <[Submodule.]SettingName,Value>> TODO, jsonify V
    private transient static final TypeToken<HashMap<String, HashMap<String,String>>> MOD_SETTINGS_TYPE = new TypeToken<>(){};

    private transient SourceProvider source;
    private transient Props props;

    private HashSet<String> enabledModules;
    // private HashSet<String> enabledSubModules;      // submodules has module.submodule notation, no effect when parent module is disabled
    private HashSet<String> commandBlacklist;
    private HashSet<String> commandWhitelist;    // Whitelist has precedence.

    private Snowflake guildId;
    private String prefix;


    // Read from resource
    public static Mono<GuildConfig> load(Snowflake guildId, SourceProvider source, Props props){
        String path = "guilds/" + guildId.asString() + "/";
        return Mono.zip(
            source.read(path + "guild", GuildConfig.class),
            source.read(path + "module_settings", MOD_SETTINGS_TYPE),
            GuildConfig::setModulesSettings
        ).map(config ->
            config.setSource(source)
                .setProps(props)
                .setId(config.getId() == null ?
                    guildId :
                    config.getId())
                .setPrefix(config.getPrefix() == null ?
                    props.get("bot.prefix") :
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
            source.write(path + "module_settings", modulesSettings)
        ).thenReturn(this);
    }
    public GuildConfig setPrefix(String prefix) {
        this.prefix = prefix.replace(' ', '_');
        return this;
    }

    public void addEnabledModule(String moduleName) {
        this.enabledModules.add(moduleName.toLowerCase());
    }

    public void removeEnabledModule(String moduleName) {
        this.enabledModules.remove(moduleName.toLowerCase());
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

    private GuildConfig setProps(Props props){
        this.props = props;
        return this;
    }

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

    public Props getProps() {
        return props;
    }
}
