package lyrth.makanism.bot.handlers;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.api.object.CommandCtx;
import lyrth.makanism.common.file.IModuleHandler;
import lyrth.makanism.common.file.config.BotConfig;
import lyrth.makanism.common.file.config.GuildConfig;
import lyrth.makanism.common.file.config.ModuleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ModuleHandler  implements IModuleHandler {
    private static final Logger log = LoggerFactory.getLogger(ModuleHandler.class);

    // lowercase keys is enforced, both maps are unmodifiable
    private static final Map<String, GuildModule<?>> guildModules;
    private static final Map<String, GuildModuleCommand<GuildModule<?>>> guildModuleCmds;

    static {
        @SuppressWarnings("rawtypes")
        ServiceLoader<GuildModule> loader = ServiceLoader.load(GuildModule.class);

        Map<String,GuildModule<?>> guildModuleMap = loader.stream().map(ServiceLoader.Provider::get)
            .collect(HashMap::new, (map, module) -> map.put(module.getName().toLowerCase(), module), (a,b)->{});

        log.debug("Loaded {} modules.", guildModuleMap.size());

        Map<String, GuildModuleCommand<GuildModule<?>>> guildModuleCmdsMap = new HashMap<>();
        for (GuildModule<?> module : guildModuleMap.values()) {
            for (Class<GuildModuleCommand<GuildModule<?>>> commandClass : module.getModuleCommands()) {
                GuildModuleCommand<GuildModule<?>> command;
                try {
                    command = commandClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.error("Error instantiating {} from {}! {}", commandClass.getSimpleName(), module.getName(), e.getMessage());
                    continue;
                }
                guildModuleCmdsMap.putIfAbsent(command.getName().toLowerCase(), command);
                for (String name : command.getAliases())
                    guildModuleCmdsMap.putIfAbsent(name.toLowerCase(), command);
            }
        }

        guildModules = Collections.unmodifiableMap(guildModuleMap);
        guildModuleCmds = Collections.unmodifiableMap(guildModuleCmdsMap);
    }

    @Override
    public Mono<?> handle(GatewayDiscordClient client, BotConfig config){
        return Flux.fromIterable(guildModules.values())
            .flatMap(guildModule -> guildModule.init(client, config))
            .then();
    }

    @Override
    public Mono<?> handleCommand(MessageCreateEvent event, BotConfig config, String invokedName){
        GuildModuleCommand<GuildModule<?>> command = guildModuleCmds.get(invokedName.toLowerCase());
        if (command == null) return Mono.empty();

        GuildModule<?> module = guildModules.get(command.getParentModuleName().toLowerCase());
        if (module == null) return Mono.empty();

        return command
            .allows(event.getMember().orElse(null), null)       // Only allow in a guild.
            .filter(b -> event.getGuildId().map(module::isEnabledFor).orElse(false))
            .flatMap(allowed -> allowed ?
                command.execute(CommandCtx.from(event, config, invokedName.toLowerCase()), module) :
                event.getMessage().getChannel().flatMap(ch -> ch.createMessage("You are not allowed to run this!"))
            )
            .doOnError(t -> log.error("CAUgHt eWWoW!", t))
            .onErrorResume(t -> event.getMessage().getChannel()
                .flatMap(ch -> CommandHandler.sendError(t, ch)));
    }

    @Override
    public void setupModulesFor(GuildConfig config){  // called in GuildConfigs orGuildHandler, once
        config.getEnabledModules().forEach(moduleName ->
            config.enableModule(moduleName).ifPresentOrElse(b -> {}, () -> log.warn("{} module not found", moduleName)));
    }

    // returns true: success, false: already enabled, empty: module not found
    // WARNING: use BotConfig/GuildConfig#disable(Guild)Module instead.
    @Override
    public Optional<Boolean> enable(String moduleName, GuildConfig config){  // can pass module.submodule notation
        log.debug("Enabling {} for {}", moduleName, config.getId().asString());
        return Optional.ofNullable(guildModules.get(moduleName.toLowerCase()))
            .map(guildModules -> guildModules.register(config));
    }

    // returns true: success, false: already disabled, empty: module not found
    // WARNING: use BotConfig/GuildConfig#disable(Guild)Module instead.
    @Override
    public Optional<Boolean> disable(String moduleName, Snowflake guildId){
        log.debug("Disabling {} for {}", moduleName, guildId.asString());
        return Optional.ofNullable(guildModules.get(moduleName.toLowerCase()))
            .map(guildModules -> guildModules.remove(guildId));
    }

    // Lowercase keys.
    public Map<String, GuildModule<?>> getGuildModules() {
        return guildModules;
    }

    // Lowercase keys.
    public Map<String, GuildModuleCommand<GuildModule<?>>> getGuildModuleCmds() {
        return guildModuleCmds;
    }

    @Override
    public Class<? extends ModuleConfig> getModuleConfigClass(String moduleName) {
        return guildModules.get(moduleName.toLowerCase()).getConfigClass();
    }

    @Override
    public Set<String> getModuleNames() {
        return guildModules.keySet();
    }
}
