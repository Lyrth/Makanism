package lyrth.makanism.api;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.gateway.ShardInfo;
import discord4j.rest.util.Snowflake;
import lyrth.makanism.common.util.file.config.BotConfig;
import lyrth.makanism.common.util.file.config.GuildConfig;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Optional;

public class CommandCtx {
    private MessageCreateEvent event;
    private Args args;
    private BotConfig config;
    private String invokedName;

    public static CommandCtx from(MessageCreateEvent event, BotConfig config, String invokedName){
        return new CommandCtx(event, config, invokedName);
    }

    private CommandCtx(MessageCreateEvent event, BotConfig config, String invokedName){
        this.event = event;
        this.config = config;
        this.invokedName = invokedName;
    }

    public GatewayDiscordClient getClient() {
        return event.getClient();
    }

    public BotConfig getBotConfig() {
        return config;
    }

    public GuildConfig getGuildConfig(){
        return getGuildId().isPresent() ? config.getGuildConfig(getGuildId().get()) : null;
    }

    public GuildConfig setModuleSetting(String module, String key, String value){
        return getGuildId().isPresent() ?
            config.getGuildConfig(getGuildId().get()).setModuleSetting(module, key, value) : null;
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public String getContent() {
        return event.getMessage().getContent();
    }

    public Mono<MessageChannel> getChannel(){
        return event.getMessage().getChannel();
    }

    public Snowflake getChannelId(){
        return event.getMessage().getChannelId();
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

    public Optional<User> getUser(){
        return event.getMessage().getAuthor();
    }

    public Instant getTimestamp(){
        return event.getMessage().getTimestamp();
    }

    public ShardInfo getShardInfo(){
        return event.getShardInfo();
    }

    public Args getArgs() {
        return (args == null) ?
            args = new Args(getContent().substring(getContent().toLowerCase().indexOf(invokedName))) :
            args;
    }
}
