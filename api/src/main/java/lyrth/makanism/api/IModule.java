package lyrth.makanism.api;

import discord4j.core.GatewayDiscordClient;
import lyrth.makanism.common.util.file.config.BotConfig;
import reactor.core.publisher.Mono;

public interface IModule {

    String getName();
    String getDesc();
    Class<? extends Command>[] getModuleCommands();

    Mono<Void> init(GatewayDiscordClient client, BotConfig config);

}
