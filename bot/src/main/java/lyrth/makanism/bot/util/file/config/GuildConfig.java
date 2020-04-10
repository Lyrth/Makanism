package lyrth.makanism.bot.util.file.config;

import com.google.gson.reflect.TypeToken;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.bot.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.util.annotation.Nullable;

import java.util.HashMap;
import java.util.HashSet;

public class GuildConfig {
    private static final Logger log = LoggerFactory.getLogger(GuildConfig.class);

    private transient HashMap<String, HashMap<String,String>> modulesSettings;  // TODO, jsonify V
    private transient static final TypeToken<HashMap<String, HashMap<String,String>>> MOD_SETTINGS_TYPE = new TypeToken<>(){};

    private HashSet<String> enabledModules;
    private HashSet<String> blacklistedCommands;
    private HashSet<String> whitelistedCommands;  // Whitelist has precedence.

    private Snowflake guildId;
    private String prefix;

    private SourceProvider source;

    // Read from resource
    public static Mono<GuildConfig> load(Snowflake guildId, SourceProvider source){
        String path = "guilds/" + guildId.asString() + "/";
        return Mono.zip(
            source.read(path + "guild", GuildConfig.class),
            source.read(path + "module_settings", MOD_SETTINGS_TYPE),
            GuildConfig::setModulesSettings
        );
    }

    // Write to resource
    public Mono<Void> update(){
        String path = "guilds/" + guildId.asString() + "/";
        return Mono.when(
            source.write(path + "guild", this),
            source.write(path + "module_settings", modulesSettings)
        );
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

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Snowflake getGuildId() {
        return guildId;
    }
}
