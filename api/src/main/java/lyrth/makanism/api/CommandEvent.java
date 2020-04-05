package lyrth.makanism.api;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.Snowflake;
import reactor.core.publisher.Mono;

import java.util.Optional;

public class CommandEvent {
    private MessageCreateEvent event;
    // private BotSettings
    private Args args;

    public GatewayDiscordClient getClient() {
        return event.getClient();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public Mono<Guild> getGuild() {
        return event.getGuild();
    }

    public Optional<Snowflake> getGuildId() {
        return event.getGuildId();
    }

    public Optional<Member> getMember() {
        return event.getMember();
    }

    public ShardInfo getShardInfo(){
        return event.getShardInfo();
    }

    public Args getArgs() {
        return (args == null) ?
            args = new Args(getMessage().getContent()) :
            args;
    }
}
