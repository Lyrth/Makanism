package lyrth.makanism.bot.handlers;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.common.util.Snowflake;
import lyrth.makanism.api.util.CommandCtx;
import lyrth.makanism.api.GuildModule;
import lyrth.makanism.api.GuildModuleCommand;
import lyrth.makanism.common.util.file.IModuleHandler;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.config.GuildConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class ModuleHandler  implements IModuleHandler {
    private static final Logger log = LoggerFactory.getLogger(ModuleHandler.class);

    // lowercase keys is enforced, both maps should be unmodifiable
    private final Map<String, GuildModule> guildModules;
    private final Map<String, GuildModuleCommand<GuildModule>> guildModuleCmds;

    public static Mono<ModuleHandler> load(){   // once, should be called first, should be quick-completing
        ServiceLoader<GuildModule> loader = ServiceLoader.load(GuildModule.class);

        return Flux.fromIterable(loader)    // Wow didn't know that I can do this... ServiceLoader is an iterable!?
            .collectMap(                    // Oh and so ServiceLoader also instantiates the classes already? :o
                /* Key */ module -> module.getName().toLowerCase(),             // *happy dragon noises*
                /* Val */ module -> module,
                /* Supplier*/ HashMap::new
            ).map(ModuleHandler::new);
    }

    public Mono<?> handle(GatewayDiscordClient client, BotConfig config){
        return Flux.fromIterable(guildModules.values())
            .flatMap(guildModule -> guildModule.init(client, config))
            .then();
    }

    public Mono<?> handleCommand(MessageCreateEvent event, BotConfig config, String invokedName){
        GuildModuleCommand<GuildModule> command = guildModuleCmds.get(invokedName.toLowerCase());
        if (command == null) return Mono.empty();

        GuildModule module = guildModules.get(command.getParentModuleName().toLowerCase());
        if (module == null) return Mono.empty();

        return command    // todo check # of args
            .allows(event.getMember().orElse(null), null)       // Only allow in a guild.
            .filter(b -> event.getGuildId().map(module::isEnabledFor).orElse(false))
            .flatMap(allowed -> allowed ?
                command.execute(CommandCtx.from(event, config, invokedName.toLowerCase()), module) :
                event.getMessage().getChannel().flatMap(ch -> ch.createMessage("You are not allowed to run this!"))
            )
            .doOnError(t -> log.error("CAUgHt eWWoW!", t))
            .onErrorResume(t -> event.getMessage().getChannel()
                .flatMap(ch -> ch.createMessage("Oh no, an error has occurred! ``` " + t.toString() + "```")));
    }

    public ModuleHandler(Map<String,GuildModule> guildModules){
        this.guildModules = Collections.unmodifiableMap(guildModules);
        Map<String, GuildModuleCommand<GuildModule>> guildModuleCommands = new HashMap<>();
        for (GuildModule module : guildModules.values()) {
            for (Class<GuildModuleCommand<GuildModule>> commandClass : module.getModuleCommands()) {
                GuildModuleCommand<GuildModule> command;
                try {
                     command = commandClass.getConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    log.error("Error instantiating {} from {}! {}", commandClass.getSimpleName(), module.getName(), e.getMessage());
                    continue;
                }
                guildModuleCommands.putIfAbsent(command.getName().toLowerCase(), command);
                for (String name : command.getAliases()){
                    guildModuleCommands.putIfAbsent(name.toLowerCase(), command);
                }
            }
        }
        this.guildModuleCmds = Collections.unmodifiableMap(guildModuleCommands);
    }

    public void setupModulesFor(GuildConfig config){  // called in GuildConfigs orGuildHandler, once
        config.getEnabledModules().forEach(moduleName ->
            enable(moduleName, config).ifPresentOrElse(b -> {}, () -> log.warn("{} module not found", moduleName)));
    }

    // returns true: success, false: already enabled, empty: module not found
    public Optional<Boolean> enable(String moduleName, GuildConfig config){  // can pass module.submodule notation
        log.debug("Enabling {} for {}", moduleName, config.getId().asString());
        return Optional.ofNullable(guildModules.get(moduleName.toLowerCase()))
            .map(guildModules -> guildModules.register(config));
    }

    // returns true: success, false: already enabled, empty: module not found
    public Optional<Boolean> disable(String moduleName, Snowflake guildId){
        log.debug("Disabling {} for {}", moduleName, guildId.asString());
        return Optional.ofNullable(guildModules.get(moduleName.toLowerCase()))
            .map(guildModules -> guildModules.remove(guildId));
    }
}
