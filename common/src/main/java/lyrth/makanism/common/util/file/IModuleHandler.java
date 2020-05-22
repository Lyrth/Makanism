package lyrth.makanism.common.util.file;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.common.util.Snowflake;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.config.GuildConfig;
import reactor.core.publisher.Mono;

import java.util.Optional;

public interface IModuleHandler {

    void setupModulesFor(GuildConfig config);

    Mono<?> handleCommand(MessageCreateEvent event, BotConfig config, String invokedName);

    Optional<Boolean> enable(String moduleName, GuildConfig config);
    Optional<Boolean> disable(String moduleName, Snowflake guildId);

    Mono<?> handle(GatewayDiscordClient client, BotConfig config);
}
