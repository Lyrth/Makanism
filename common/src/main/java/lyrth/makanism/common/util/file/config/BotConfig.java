package lyrth.makanism.common.util.file.config;

import discord4j.rest.util.Snowflake;
import lyrth.makanism.common.util.file.Props;
import lyrth.makanism.common.util.file.SourceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.HashSet;

public class BotConfig {
    private static final Logger log = LoggerFactory.getLogger(BotConfig.class);

    private transient HashMap<Snowflake, GuildConfig> guildConfigs;

    private transient SourceProvider source;
    private transient Props props;

    private String defaultPrefix;    // only modifiable in json
    private Snowflake botId;
    private Snowflake ownerId;

    private HashSet<String> disabledCommands;   // global blacklist


    public static Mono<BotConfig> load(SourceProvider source, HashMap<Snowflake, GuildConfig> guildConfigs, Props props){
        String path = "bot/bot";
        return source.read(path, BotConfig.class).map(config -> config.setSource(source).setGuildConfigs(guildConfigs).setProps(props));
    }

    public static BotConfig lazyLoad(SourceProvider source, Props props){  // in order to setup guildConfigs first
        BotConfig config = new BotConfig();
        config.setProps(props);
        config.setSource(source);
        config.setGuildConfigs(new HashMap<>());
        return config;
    }

    public Mono<BotConfig> load(){
        String path = "bot/bot";
        return source.read(path, BotConfig.class)
            .map(config -> {
                config.setSource(this.source);
                config.setProps(this.props);
                config.setGuildConfigs(this.guildConfigs);
                if (config.getDefaultPrefix() == null) config.setDefaultPrefix(props.get("bot.prefix"));
                config.setDefaultPrefix(config.getDefaultPrefix());  // remove spaces :P
                if (config.getDisabledCommands() == null) config.setDisabledCommands(new HashSet<>());
                return config;
            });
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

    public BotConfig putGuildConfig(GuildConfig config){
        guildConfigs.put(config.getId(),config);
        return this;
    }

    public BotConfig setIds(Snowflake botId, Snowflake ownerId) {
        this.botId = botId;
        this.ownerId = ownerId;
        return this;
    }

    private BotConfig setSource(SourceProvider source){
        this.source = source;
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

    public GuildConfig getGuildConfig(Snowflake guildId) {
        return guildConfigs.get(guildId);
    }

    public SourceProvider getSourceProvider() {
        return source;
    }

}
