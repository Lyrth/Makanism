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
import reactor.core.publisher.DirectProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ModuleHandler  implements IModuleHandler {
    private static final Logger log = LoggerFactory.getLogger(ModuleHandler.class);

    // lowercase keys is enforced, both maps are unmodifiable
    private final Map<String, GuildModule<?>> guildModules;
    private final Map<String, GuildModuleCommand<GuildModule<?>>> guildModuleCmds;

    private final DirectProcessor<Object> unhandleProcessor = DirectProcessor.create();
    private final FluxSink<Object> unhandleSink = unhandleProcessor.sink(FluxSink.OverflowStrategy.BUFFER);

    /* Mutable */
    private URLClassLoader loader;

    private ModuleHandler(Map<String, GuildModule<?>> guildModules, Map<String, GuildModuleCommand<GuildModule<?>>> guildModuleCmds, URLClassLoader loader){
        this.guildModules = guildModules;
        this.guildModuleCmds = guildModuleCmds;
        this.loader = loader;
    }

    public static Mono<ModuleHandler> create(){
        File[] jars = new File("modules/").listFiles(s -> s.getName().endsWith(".jar"));
        jars = jars != null ? jars : new File[0];
        URLClassLoader urlLoader = new URLClassLoader(
            Arrays.stream(jars)
                .map(f -> {
                    try {
                        return f.toURI().toURL();
                    } catch (MalformedURLException e) {
                        log.error("Error converting to url: ", e);
                        return null;
                    }
                })
                .toArray(URL[]::new)
        );
        @SuppressWarnings("rawtypes")
        ServiceLoader<GuildModule> loader = ServiceLoader.load(GuildModule.class, urlLoader);

        Map<String,GuildModule<?>> guildModuleMap = loader.stream()
            .map(ServiceLoader.Provider::get)
            .peek(module -> log.debug("Found GuildModule class {}", module.getClass().getName()))
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

        return Mono.just(
            //new ModuleHandler(Collections.unmodifiableMap(guildModuleMap), Collections.unmodifiableMap(guildModuleCmdsMap), urlLoader)
            new ModuleHandler(guildModuleMap, guildModuleCmdsMap, urlLoader)
        );
    }

    @Override
    public Mono<?> handle(GatewayDiscordClient client, BotConfig config){
        return Flux.defer(() -> Flux.fromIterable(guildModules.values()))
            .doOnNext(guildModule -> log.info("Init for {}@{}", guildModule.getClass().getName(), System.identityHashCode(guildModule)))
            .flatMap(guildModule -> guildModule.init(client, config))
            .takeUntilOther(unhandleProcessor)
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
    public Mono<Void> unload(){
        return Flux.defer(() -> Flux.fromIterable(guildModules.values()))
            .flatMap(GuildModule::uninit)
            .then(Mono.fromCallable(() -> {
                guildModuleCmds.clear();
                guildModules.clear();
                unhandleSink.next(0);
                loader.close();
                return 0;
            }))
            .then();
    }

    @Override
    public Mono<Void> reload(GatewayDiscordClient client, BotConfig config){
        return Mono.fromCallable(() -> {
            File[] jars = new File("modules/").listFiles(s -> s.getName().endsWith(".jar"));
            jars = jars != null ? jars : new File[0];
            this.loader = new URLClassLoader(
                Arrays.stream(jars)
                    .map(f -> {
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            log.error("Error converting to url: ", e);
                            return null;
                        }
                    })
                    .toArray(URL[]::new)
            );
            @SuppressWarnings("rawtypes")
            ServiceLoader<GuildModule> loader = ServiceLoader.load(GuildModule.class, this.loader);

            loader.stream()
                .map(ServiceLoader.Provider::get)
                .peek(module -> log.debug("Found GuildModule class {}", module.getClass().getName()))
                .collect(() -> guildModules, (map, module) -> map.put(module.getName().toLowerCase(), module), (a,b)->{});

            log.debug("Loaded {} modules.", guildModules.size());

            for (GuildModule<?> module : guildModules.values()) {
                for (Class<GuildModuleCommand<GuildModule<?>>> commandClass : module.getModuleCommands()) {
                    GuildModuleCommand<GuildModule<?>> command;
                    try {
                        command = commandClass.getConstructor().newInstance();
                    } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                        log.error("Error instantiating {} from {}! {}", commandClass.getSimpleName(), module.getName(), e.getMessage());
                        continue;
                    }
                    guildModuleCmds.putIfAbsent(command.getName().toLowerCase(), command);
                    for (String name : command.getAliases())
                        guildModuleCmds.putIfAbsent(name.toLowerCase(), command);
                }
            }

            return 0;
        }).then(Mono.when(
            handle(client, config),
            Mono.fromRunnable(() -> config.getGuildConfigs().values().forEach(this::setupModulesFor))
        ));
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
