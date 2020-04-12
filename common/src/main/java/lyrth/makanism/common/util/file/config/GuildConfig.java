package lyrth.makanism.common.util.file.config;

import com.google.gson.reflect.TypeToken;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.common.util.file.Props;
import lyrth.makanism.common.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class GuildConfig {    // TODO not null fields
    private static final Logger log = LoggerFactory.getLogger(GuildConfig.class);

    private transient HashMap<String, HashMap<String,String>> modulesSettings;  // TODO, jsonify V
    private transient static final TypeToken<HashMap<String, HashMap<String,String>>> MOD_SETTINGS_TYPE = new TypeToken<>(){};

    private transient SourceProvider source;
    private transient Props props;

    private HashSet<String> enabledModules;
    private HashSet<String> blacklistedCommands;
    private HashSet<String> whitelistedCommands;  // Whitelist has precedence.

    private Snowflake guildId;
    private String prefix;


    // Read from resource
    public static Mono<GuildConfig> load(Snowflake guildId, SourceProvider source, Props props){
        String path = "guilds/" + guildId.asString() + "/";
        return Mono.zip(
            source.read(path + "guild", GuildConfig.class),
            source.read(path + "module_settings", MOD_SETTINGS_TYPE),
            GuildConfig::setModulesSettings
        )
            .map(config -> {
                config.setSource(source);
                config.setProps(props);
                if (config.getId() == null) config.setId(guildId);
                if (config.getPrefix() == null) config.setPrefix(props.get("bot.prefix"));
                config.setPrefix(config.getPrefix());  // remove spaces :P
                if (config.getWhitelistedCommands() == null) config.setWhitelistedCommands(new HashSet<>());
                if (config.getBlacklistedCommands() == null) config.setBlacklistedCommands(new HashSet<>());
                if (config.getEnabledModules() == null) config.setEnabledModules(new HashSet<>());
                return config;
            });
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

    private GuildConfig setId(Snowflake id){
        this.guildId = id;
        return this;
    }

    private GuildConfig setEnabledModules(HashSet<String> enabledModules) {
        this.enabledModules = enabledModules;
        return this;
    }

    private GuildConfig setBlacklistedCommands(HashSet<String> blacklistedCommands) {
        this.blacklistedCommands = blacklistedCommands;
        return this;
    }

    private GuildConfig setWhitelistedCommands(HashSet<String> whitelistedCommands) {
        this.whitelistedCommands = whitelistedCommands;
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

    public HashSet<String> getBlacklistedCommands() {
        return blacklistedCommands;
    }

    public HashSet<String> getWhitelistedCommands() {
        return whitelistedCommands;
    }

    public String getPrefix() {
        return prefix;
    }

    public Snowflake getId() {
        return guildId;
    }
}
